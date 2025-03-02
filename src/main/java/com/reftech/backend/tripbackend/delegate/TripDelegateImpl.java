package com.reftech.backend.tripbackend.delegate;

import com.reftech.backend.tripbackend.api.*;
import com.reftech.backend.tripbackend.service.TripService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@AllArgsConstructor
public class TripDelegateImpl implements TripsApiDelegate {

    private final TripService tripService;

    public Mono<ResponseEntity<Trip>> createTrip(Mono<TripRequest> tripRequest,
                                                  ServerWebExchange exchange) {
        return tripService
                .createTrip(tripRequest)
                .map(trip -> ResponseEntity.status(HttpStatus.CREATED).body(trip));
    }

    public Mono<ResponseEntity<Void>> deleteTrip(UUID id,
                                                  ServerWebExchange exchange) {
        return tripService
                .deleteTrip(id)
                .map(trip -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    public Mono<ResponseEntity<Trip>> getTripById(UUID id,
                                                   ServerWebExchange exchange) {
        return tripService
                .getTripById(id)
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<Flux<TripSummary>>> getTrips(ServerWebExchange exchange) {
        return Mono
                .just(ResponseEntity
                        .ok(tripService
                                .getTrips()));
    }

    public Mono<ResponseEntity<Trip>> updateTrip(UUID id,
                                                  Mono<TripUpdateRequest> tripUpdateRequest,
                                                  ServerWebExchange exchange) {
        return tripService
                .updateTrip(id, tripUpdateRequest)
                .map(ResponseEntity::ok);
    }

}
