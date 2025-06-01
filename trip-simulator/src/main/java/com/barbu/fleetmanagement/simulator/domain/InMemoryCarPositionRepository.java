package com.barbu.fleetmanagement.simulator.domain;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryCarPositionRepository implements CarPositionRepository {

    private final Map<Long, CarPosition> activeTrips = new ConcurrentHashMap<>();

    @Override
    public void save(CarPosition carPosition) {
        activeTrips.put(carPosition.getTripId(), carPosition);
    }

    @Override
    public void deleteByTripId(Long tripId) {
        activeTrips.remove(tripId);
    }

    @Override
    public Optional<CarPosition> findByTripId(Long tripId) {
        return Optional.ofNullable(activeTrips.get(tripId));
    }
}
