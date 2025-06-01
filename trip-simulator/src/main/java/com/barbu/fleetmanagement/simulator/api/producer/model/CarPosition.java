package com.barbu.fleetmanagement.simulator.api.producer.model;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Location;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CarPosition {
    private final Long carId;
    private final Long driverId;
    private final Long tripId;
    private final Location currentLocation;
    private final Instant timestamp;
}
