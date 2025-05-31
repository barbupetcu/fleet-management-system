package com.barbu.fleetmanagement.manager.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record Trip(@Valid @NotNull Location start,
                  @Valid @NotNull Location destination,
                  @NotNull Long driverId,
                  @NotNull Long carId) {
}