package com.barbu.fleetmanagement.common.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record Trip(Long id,
                  @Valid @NotNull Location start,
                  @Valid @NotNull Location destination,
                  @NotNull Long driverId,
                  @NotNull Long carId) {
}