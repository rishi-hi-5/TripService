package com.reftech.backend.tripbackend.repository;

import com.reftech.backend.tripbackend.model.TripStopEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TripStopRepository extends ReactiveCrudRepository<TripStopEntity, UUID> {
    Flux<TripStopEntity> findByTripId(UUID tripId);
}