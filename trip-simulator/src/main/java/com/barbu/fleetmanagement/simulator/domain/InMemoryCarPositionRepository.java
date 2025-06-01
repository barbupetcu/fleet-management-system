package com.barbu.fleetmanagement.simulator.domain;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryCarPositionRepository implements CarPositionRepository {

    private final Map<Long, CarPositionDetails> activeTrips = new ConcurrentHashMap<>();

    @Override
    public void save(CarPositionDetails carPositionDetails) {
        activeTrips.put(carPositionDetails.getTripId(), carPositionDetails);
    }

    @Override
    public void deleteByTripId(Long tripId) {
        activeTrips.remove(tripId);
    }

    @Override
    public Optional<CarPositionDetails> findByTripId(Long tripId) {
        return Optional.ofNullable(activeTrips.get(tripId));
    }
}
