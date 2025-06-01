package com.barbu.fleetmanagement.penaltypoints.domain;

import com.barbu.fleetmanagement.common.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the calculated speed of a car between two positions.
 */
@Getter
@Builder
public class CarSpeed {
    private final Long carId;
    private final Long driverId;
    private final Long tripId;
    private final Location currentLocation;
    private final Location previousLocation;
    private final Instant currentTimestamp;
    private final Instant previousTimestamp;
    private final BigDecimal speedKmh;
    private final BigDecimal distanceKm;
}