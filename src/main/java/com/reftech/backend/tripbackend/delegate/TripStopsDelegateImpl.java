package com.reftech.backend.tripbackend.delegate;

import com.reftech.backend.tripbackend.api.TripStop;
import com.reftech.backend.tripbackend.api.TripStopUpdateRequest;
import com.reftech.backend.tripbackend.api.TripStopsApiDelegate;
import com.reftech.backend.tripbackend.service.TripService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@AllArgsConstructor
public class TripStopsDelegateImpl implements TripStopsApiDelegate {
    private final TripService tripService;
    public Mono<ResponseEntity<Void>> addTripStops(UUID id,
                                                    Flux<TripStop> tripStop,
                                                    ServerWebExchange exchange) {
        return tripService
                .addTripStops(id, tripStop)
                .map(trip -> ResponseEntity.status(HttpStatus.CREATED).build());
    }

    public Mono<ResponseEntity<Void>> deleteTripStop(UUID tripId,
                                                      UUID stopId,
                                                      ServerWebExchange exchange) {
        return tripService
                .deleteTripStop(tripId, stopId)
                .map(trip -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    public Mono<ResponseEntity<TripStop>> updateTripStop(UUID tripId,
                                                          UUID stopId,
                                                          Mono<TripStopUpdateRequest> tripStopUpdateRequest,
                                                          ServerWebExchange exchange) {
        return tripService
                .updateTripStop(tripId, stopId, tripStopUpdateRequest)
                .map(ResponseEntity::ok);
    }
}
