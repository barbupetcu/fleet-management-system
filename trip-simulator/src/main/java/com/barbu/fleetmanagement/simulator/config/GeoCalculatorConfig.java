package com.barbu.fleetmanagement.simulator.config;

import com.barbu.fleetmanagement.common.geo.GeoCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;

@ApplicationScoped
public class GeoCalculatorConfig {
    @Produces
    @ApplicationScoped
    public GeoCalculator geoCalculator() {
        return new GeoCalculator();
    }
}
