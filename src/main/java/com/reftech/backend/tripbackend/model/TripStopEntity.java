package com.reftech.backend.tripbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@Table("trip_stops")
public class TripStopEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    public TripStopEntity(){
        this.id = UUID.randomUUID();
    }
    @Column("trip_id")
    private UUID tripId;

    private String location;

    @Column("stop_type")
    private String stopType; // ENUM: PICKUP, DROPOFF

    private Integer sequence; // Order of stop in the trip

    @Column("load_quantity")
    private Double loadQuantity; // Quantity loaded/unloaded at this stop

    @Column("scheduled_time")
    private LocalDateTime scheduledTime;

    @Column("actual_time")
    private LocalDateTime actualTime;

    @Override
    public boolean isNew() {
        return id==null;
    }
}
