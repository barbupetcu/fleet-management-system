package com.barbu.fleetmanagement.simulator.api.consumer.model;

import java.math.BigDecimal;

public record Location(BigDecimal latitude, BigDecimal longitude) {

    @Override
    public String toString() {
        return String.format("(%.6f, %.6f)",
                this.latitude().doubleValue(),
                this.longitude().doubleValue());
    }
}