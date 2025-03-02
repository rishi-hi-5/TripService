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
@Table("trips")
public class TripEntity implements Persistable<UUID> {
    @Id
    private UUID id;

    public TripEntity(){
        this.id = UUID.randomUUID();
    }

    @Column("order_id")
    private UUID orderId;

    @Column("driver_id")
    private UUID driverId;

    @Column("vehicle_id")
    private UUID vehicleId;

    private String status; // ENUM: PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELED, FAILED

    private Double capacity; // Total capacity of the vehicle
    @Column("load_type")
    private String loadType; // ENUM: PARTIAL_LOAD, DEDICATED_LOAD
    @Column("source")
    private String source;

    @Column("destination")
    private String destination;
    @Column("scheduled_time")
    private LocalDateTime scheduledTime;

    @Column("start_time")
    private LocalDateTime startTime;

    @Column("end_time")
    private LocalDateTime endTime;

    @Override
    public boolean isNew() {
        return id==null;
    }
}
