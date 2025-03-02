package com.reftech.backend.tripbackend.service;

import com.reftech.backend.tripbackend.api.*;
import com.reftech.backend.tripbackend.mapper.TripMapper;
import com.reftech.backend.tripbackend.mapper.TripStopsMapper;
import com.reftech.backend.tripbackend.model.TripEntity;
import com.reftech.backend.tripbackend.model.TripStopEntity;
import com.reftech.backend.tripbackend.repository.TripRepository;
import com.reftech.backend.tripbackend.repository.TripStopRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    
    private final TripStopRepository tripStopRepository;
    private final TripStopsMapper tripStopMapper;
    public Mono<Trip> createTrip(Mono<TripRequest> tripRequestMono) {
        return tripRequestMono.flatMap(tripRequest -> tripRepository
                .save(tripMapper.toEntity(tripRequest))
                .map(tripMapper::toDto));
    }

    public Mono<Void> deleteTrip(UUID id) {
        return tripRepository
                .findById(id)
                .map(trip -> {
                    trip.setStatus("CANCELED");
                    trip.setStartTime(LocalDateTime.now());
                    trip.setEndTime(LocalDateTime.now());
                    return trip;
                })
                .flatMap(tripRepository::save)
                .then();
    }


    public Mono<Trip> getTripById(UUID id) {
        return tripRepository
                .findById(id)
                .map(tripMapper::toDto);
    }

    public Flux<TripSummary> getTrips() {
        return tripRepository
                .findAll()
                .map(tripMapper::toTripSummary);
    }

    public Mono<Trip> updateTrip(UUID id, Mono<TripUpdateRequest> tripUpdateRequest) {
        return tripRepository
                .findById(id)
                .zipWith(tripUpdateRequest)
                .map(data -> {
                    TripEntity tripEntity = data.getT1();
                    TripUpdateRequest updateRequest = data.getT2();
                    tripEntity.setDriverId(updateRequest.getDriverId());
                    tripEntity.setVehicleId(updateRequest.getVehicleId());
                    tripEntity.setStatus(updateRequest.getStatus().getValue());

                    if(tripEntity.getStatus().equals("COMPLETED")){
                        tripEntity.setEndTime(LocalDateTime.now());
                    }else if(tripEntity.getStatus().equals("IN_PROGRESS")){
                        tripEntity.setStartTime(LocalDateTime.now());
                    }

                    return tripEntity;
                })
                .flatMap(tripRepository::save)
                .map(tripMapper::toDto);
    }

    public Mono<Void> addTripStops(UUID id, Flux<TripStop> tripStop) {
        return tripRepository
                .existsById(id)
                .zipWith(tripStop.collectList())
                .flatMap(data -> {
                    if(data.getT1()){
                        return tripStopRepository
                                .saveAll(data
                                        .getT2()
                                        .parallelStream()
                                        .map(tripStopMapper::toEntity)
                                        .peek(tripStopEntity -> tripStopEntity.setTripId(id))
                                        .toList())
                                .then();
                    }else{
                        return Mono.error(new RuntimeException("Trip not found"));
                    }
                });
    }

    public Mono<Void> deleteTripStop(UUID tripId, UUID stopId) {
        return tripRepository
                .existsById(tripId)
                .flatMap(exists -> {
                    if(exists){
                        return tripStopRepository
                                .deleteById(stopId)
                                .then();
                    }else{
                        return Mono.error(new RuntimeException("Trip not found"));
                    }
                });
    }

    public Mono<TripStop> updateTripStop(UUID tripId, UUID stopId, Mono<TripStopUpdateRequest> tripStopUpdateRequest) {
        return tripRepository
                .existsById(tripId)
                .flatMap(exists -> {
                    if(exists){
                        return tripStopRepository
                                .findById(stopId)
                                .zipWith(tripStopUpdateRequest)
                                .map(data -> {
                                    TripStopEntity tripStop = data.getT1();
                                    TripStopUpdateRequest updateRequest = data.getT2();
                                    tripStop.setActualTime(LocalDateTime.parse(updateRequest.getActualTime()));
                                    return tripStop;
                                })
                                .flatMap(tripStopRepository::save);
                    }else{
                        return Mono.error(new RuntimeException("Trip not found"));
                    }
                }).map(tripStopMapper::toDto);
    }
}
