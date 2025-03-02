package com.reftech.backend.tripbackend.mapper;


import com.reftech.backend.tripbackend.api.TripStop;
import com.reftech.backend.tripbackend.model.TripStopEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripStopsMapper {
    TripStop toDto(TripStopEntity tripStopEntity);

    TripStopEntity toEntity(TripStop tripStop);
}
