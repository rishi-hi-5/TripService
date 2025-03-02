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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    
    private final TripStopRepository tripStopRepository;
    private final TripStopsMapper tripStopMapper;

    @Transactional
    public Mono<Trip> createTrip(Mono<TripRequest> tripRequestMono) {
        return tripRequestMono.flatMap(tripRequest -> {
            TripEntity trip = tripMapper.toEntity(tripRequest);
            return tripRepository.save(trip)
                    .flatMap(tripResponse->{
                        UUID tripId = tripResponse.getId();
                        List<TripStopEntity> tripStops = tripRequest.getStops().stream()
                                .map(tripStopMapper::toEntity)
                                .peek(tripStop -> tripStop.setTripId(tripId))
                                .toList();

                        return tripStopRepository.saveAll(tripStops)
                                .then(tripStopRepository.findByTripId(tripId).collectList())
                                .map(savedStops -> {
                                    Trip tripDto = tripMapper.toDto(trip);
                                    tripDto.setStops(savedStops.parallelStream().map(tripStopMapper::toDto).toList());
                                    return tripDto;
                                });
                    });
        });
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
                .flatMap(trip -> tripStopRepository
                        .findByTripId(id)
                        .collectList()
                        .map(tripStops -> {
                            Trip tripDto = tripMapper.toDto(trip);
                            tripDto.setStops(tripStops.parallelStream().map(tripStopMapper::toDto).toList());
                            return tripDto;
                        }));
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
                    if(tripEntity.getStatus().equals("PENDING")) {
                        tripEntity.setSource(updateRequest.getSource());
                        tripEntity.setDestination(updateRequest.getDestination());
                    } else if(isSourceOrDesinationSameWhenNotPending(tripEntity, updateRequest)){
                        return Mono.error(new RuntimeException("Source and destination cannot be changed for a trip in progress or completed state."));
                    }

                    if(tripEntity.getStatus().equals("COMPLETED")){
                        tripEntity.setEndTime(LocalDateTime.now());
                    }else if(tripEntity.getStatus().equals("IN_PROGRESS")){
                        tripEntity.setStartTime(LocalDateTime.now());
                    }

                    return tripEntity;
                })
                .flatMap(tripData-> tripRepository.save((TripEntity) tripData))
                .zipWith(tripStopRepository.findByTripId(id).collectList())
                .map(data -> {
                    Trip tripDto = tripMapper.toDto(data.getT1());
                    tripDto.setStops(data.getT2().parallelStream().map(tripStopMapper::toDto).toList());
                    return tripDto;
                });
    }

    private static boolean isSourceOrDesinationSameWhenNotPending(TripEntity tripEntity, TripUpdateRequest updateRequest) {
        return !Objects.equals(tripEntity.getSource(), updateRequest.getSource()) || !Objects.equals(tripEntity.getDestination(), updateRequest.getDestination());
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

    public Mono<PaginatedTripSummary> getTrips(Integer page, Integer size) {
        int offset = page * size;

        return tripRepository.countTrips()
                .flatMap(totalElements -> tripRepository.findTripSummaries(size, offset)
                        .collectList()
                        .map(trips -> {
                            PaginatedTripSummary paginatedTripSummary = new PaginatedTripSummary();
                            paginatedTripSummary.setTotalElements(totalElements.intValue());
                            paginatedTripSummary.setTotalPages((int) Math.ceil((double) totalElements / size));
                            paginatedTripSummary.setCurrentPage(page);
                            paginatedTripSummary.setTrips(trips);
                            return paginatedTripSummary;
                        }));
    }
}
