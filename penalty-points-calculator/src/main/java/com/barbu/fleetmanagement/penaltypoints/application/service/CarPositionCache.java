package com.barbu.fleetmanagement.penaltypoints.application.service;

import com.barbu.fleetmanagement.common.model.CarPosition;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for storing previous car position. In a more advanced implementation should be replaced with a cache like
 * Redis having as TTL the maximum time (plus a threshold) between each hearth beat of the same trip
 */
@ApplicationScoped
public class CarPositionCache {

    private final Map<Long, CarPosition> previousPositions = new ConcurrentHashMap<>();

    public CarPosition getByTripId(Long tripId) {
        return previousPositions.get(tripId);
    }

    public void save(CarPosition carPosition) {
        previousPositions.put(carPosition.tripId(), carPosition);
    }

}
