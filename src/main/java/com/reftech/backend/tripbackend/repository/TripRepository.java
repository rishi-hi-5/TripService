package com.reftech.backend.tripbackend.repository;

import com.reftech.backend.tripbackend.model.TripEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TripRepository extends ReactiveCrudRepository<TripEntity, UUID> {
}
