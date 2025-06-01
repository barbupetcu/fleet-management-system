package com.barbu.fleetmanagement.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record Location(
        @NotNull @Min(value = -90) @Max(value = 90) BigDecimal latitude,
        @NotNull @Min(value = -180) @Max(value = 180) BigDecimal longitude) {
}
