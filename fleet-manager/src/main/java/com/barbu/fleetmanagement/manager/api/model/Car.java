package com.barbu.fleetmanagement.manager.api.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record Car(Long id,
                  @NotEmpty String model,
                  @NotEmpty String brand,
                  @NotEmpty String colour,
                  @NotEmpty String plateNumber) {
}
