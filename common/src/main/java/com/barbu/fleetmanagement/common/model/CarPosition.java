package com.barbu.fleetmanagement.common.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record CarPosition(Long carId,
                          Long driverId,
                          Long tripId,
                          Location currentLocation,
                          Instant timestamp) {
}
