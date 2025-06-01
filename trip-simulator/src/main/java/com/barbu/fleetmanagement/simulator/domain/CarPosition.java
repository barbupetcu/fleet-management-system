package com.barbu.fleetmanagement.simulator.domain;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Location;
import com.barbu.fleetmanagement.simulator.api.consumer.model.Trip;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class CarPosition {
    private final Long carId;
    private final Long driverId;
    private final Long tripId;
    private final Location currentLocation;
    private final Location destination;
    private final BigDecimal speedKmPerHour;
    private final Instant timestamp;

    public static CarPosition createInitialPosition(Trip trip, Long tripId) {
        return CarPosition.builder()
                .carId(trip.carId())
                .driverId(trip.driverId())
                .tripId(tripId)
                .currentLocation(trip.start())
                .destination(trip.destination())
                .speedKmPerHour(new BigDecimal("60")) // Default speed 60 km/h
                .timestamp(Instant.now())
                .build();
    }
}