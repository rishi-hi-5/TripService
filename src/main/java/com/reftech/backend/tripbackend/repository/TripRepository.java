package com.reftech.backend.tripbackend.repository;

import com.reftech.backend.tripbackend.api.TripSummary;
import com.reftech.backend.tripbackend.model.TripEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TripRepository extends ReactiveCrudRepository<TripEntity, UUID> {
    @Query("""
        SELECT t.id,
               t.source,
               t.destination,
               t.status
        FROM trips t
        ORDER BY t.scheduled_time DESC
        LIMIT :size OFFSET :offset
    """)
    Flux<TripSummary> findTripSummaries(int size, int offset);

    @Query("SELECT COUNT(*) FROM trips")
    Mono<Long> countTrips();
}
