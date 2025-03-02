package com.reftech.backend.tripbackend.mapper;


import com.reftech.backend.tripbackend.api.Trip;
import com.reftech.backend.tripbackend.api.TripRequest;
import com.reftech.backend.tripbackend.api.TripSummary;
import com.reftech.backend.tripbackend.model.TripEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripMapper {
    Trip toDto(TripEntity trip);
    TripSummary toTripSummary(TripEntity trip);

    TripEntity toEntity(TripRequest trip);
}