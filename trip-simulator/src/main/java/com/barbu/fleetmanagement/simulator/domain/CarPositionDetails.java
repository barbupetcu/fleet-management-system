package com.barbu.fleetmanagement.simulator.domain;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Location;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class CarPositionDetails {
    private final Long carId;
    private final Long driverId;
    private final Long tripId;
    private final Location currentLocation;
    private final Location destination;
    private final BigDecimal speedKmPerHour;
    private final Instant timestamp;
}