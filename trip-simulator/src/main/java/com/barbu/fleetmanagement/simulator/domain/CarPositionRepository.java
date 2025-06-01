package com.barbu.fleetmanagement.simulator.domain;

import java.util.Optional;

//TODO add implementation for database persistence
public interface CarPositionRepository {
    void save(CarPosition carPosition);
    void deleteByTripId(Long tripId);
    Optional<CarPosition> findByTripId(Long tripId);
}
