package com.barbu.fleetmanagement.simulator.domain;

import java.util.Optional;

//TODO add implementation for database persistence
public interface CarPositionRepository {
    void save(CarPositionDetails carPositionDetails);
    void deleteByTripId(Long tripId);
    Optional<CarPositionDetails> findByTripId(Long tripId);
}
