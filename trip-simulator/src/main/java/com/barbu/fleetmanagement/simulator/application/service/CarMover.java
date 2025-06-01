package com.barbu.fleetmanagement.simulator.application.service;

import com.barbu.fleetmanagement.simulator.domain.CarPosition;
import com.barbu.fleetmanagement.simulator.api.consumer.model.Location;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class CarMover {

    private final GeoCalculator geoCalculator;

    public Optional<CarPosition> move(CarPosition position) {

        Instant now = Instant.now();
        Duration elapsed = Duration.between(position.getTimestamp(), now);
        BigDecimal elapsedSeconds = new BigDecimal(elapsed.toSeconds());

        // Calculate distance to move based on speed and elapsed time
        BigDecimal distanceToMoveKm = position.getSpeedKmPerHour()
                .multiply(elapsedSeconds)
                .divide(new BigDecimal("3600"), GeoCalculator.MATH_CONTEXT);

        // Calculate the new position
        Location newLocation = geoCalculator.calculateNewPosition(
                position.getCurrentLocation(),
                position.getDestination(),
                distanceToMoveKm);

        // Check if the car has reached the destination
        BigDecimal remainingDistance = geoCalculator.calculateDistanceInKm(newLocation, position.getDestination());
        boolean completed = remainingDistance.compareTo(new BigDecimal("0.1")) < 0;
        log.info("Updating position for trip {}: {}, remaining distance: {} km, completed: {}",
                position.getTripId(), newLocation, remainingDistance, completed);
        if (completed) {
            return Optional.empty();
        } else {
            return Optional.of(CarPosition.builder()
                    .carId(position.getCarId())
                    .driverId(position.getDriverId())
                    .tripId(position.getTripId())
                    .currentLocation(newLocation)
                    .destination(position.getDestination())
                    .speedKmPerHour(position.getSpeedKmPerHour())
                    .timestamp(now)
                    .build());
        }
    }
}
