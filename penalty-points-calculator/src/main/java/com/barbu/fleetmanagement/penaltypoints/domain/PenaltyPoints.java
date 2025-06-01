package com.barbu.fleetmanagement.penaltypoints.domain;

import lombok.Builder;

import java.time.Instant;

@Builder
public record PenaltyPoints(
        Long driverId, Long tripId,
        Long carId,
        int penaltyPoints,
        Instant penaltyTime) {
}
