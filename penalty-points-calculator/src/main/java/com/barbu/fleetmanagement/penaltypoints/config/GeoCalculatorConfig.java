package com.barbu.fleetmanagement.penaltypoints.config;

import com.barbu.fleetmanagement.common.geo.GeoCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class GeoCalculatorConfig {

    @ApplicationScoped
    @Produces
    public GeoCalculator geoCalculator() {
        return new GeoCalculator();
    }
}
