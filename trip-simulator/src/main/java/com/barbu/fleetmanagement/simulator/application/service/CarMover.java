package com.barbu.fleetmanagement.simulator.application.service;

import com.barbu.fleetmanagement.common.geo.GeoCalculator;
import com.barbu.fleetmanagement.common.model.Location;
import com.barbu.fleetmanagement.common.model.Trip;
import com.barbu.fleetmanagement.simulator.domain.CarPositionDetails;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class CarMover {


    private final static Random random = new Random();
    private final static int MINIMUM_SPED_KM_H = 60;
    private final static int MAXIMUM_SPED_KM_H = 80;

    private final GeoCalculator geoCalculator;

    public CarPositionDetails moveToInitialPosition(Trip trip) {
        return CarPositionDetails.builder()
                .carId(trip.carId())
                .driverId(trip.driverId())
                .tripId(trip.id())
                .currentLocation(trip.start())
                .destination(trip.destination())
                .speedKmPerHour(generateSpeedKmPerHour()) // Default speed 60 km/h
                .timestamp(Instant.now())
                .build();
    }

    public Optional<CarPositionDetails> move(CarPositionDetails position) {

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
        log.info("Updating position for trip {}: {}, remaining distance: {} km, actual speed: {}, completed: {}",
                position.getTripId(), newLocation, remainingDistance, position.getSpeedKmPerHour(), completed);
        if (completed) {
            return Optional.empty();
        } else {
            return Optional.of(CarPositionDetails.builder()
                    .carId(position.getCarId())
                    .driverId(position.getDriverId())
                    .tripId(position.getTripId())
                    .currentLocation(newLocation)
                    .destination(position.getDestination())
                    .speedKmPerHour(generateSpeedKmPerHour())
                    .timestamp(now)
                    .build());
        }
    }

    private BigDecimal generateSpeedKmPerHour() {
        return new BigDecimal(MINIMUM_SPED_KM_H + random.nextInt(MAXIMUM_SPED_KM_H - MINIMUM_SPED_KM_H + 1));
    }
}
