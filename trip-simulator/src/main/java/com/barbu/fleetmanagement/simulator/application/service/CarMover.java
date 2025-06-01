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

/**
 * Service responsible for simulating car movements during trips.
 * <p>
 * This class handles the calculation of car positions as they move from a starting point
 * to a destination. It simulates realistic car movement by:
 * <ul>
 *   <li>Generating random speeds within defined limits</li>
 *   <li>Calculating new positions based on elapsed time and speed</li>
 *   <li>Determining when a car has reached its destination</li>
 * </ul>
 * The service is used by the Trip Simulator to generate position updates that are published
 * to the car position Kafka topic.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class CarMover {

    private final static Random random = new Random();
    private final static int MINIMUM_SPED_KM_H = 40;
    private final static int MAXIMUM_SPED_KM_H = 120;
    private final GeoCalculator geoCalculator;

    public CarPositionDetails moveToInitialPosition(Trip trip) {
        return CarPositionDetails.builder()
                .carId(trip.carId())
                .driverId(trip.driverId())
                .tripId(trip.id())
                .currentLocation(trip.start())
                .destination(trip.destination())
                .speedKmPerHour(generateSpeedKmPerHour())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Calculates a new position for a car based on elapsed time and speed.
     * <p>
     * This method simulates the movement of a car from its current position toward its destination.
     * It calculates:
     * <ol>
     *   <li>The elapsed time since the last position update</li>
     *   <li>The distance the car should move based on its speed and elapsed time</li>
     *   <li>The new geographic position by interpolating between current location and destination</li>
     * </ol>
     * If the car has reached its destination (within 0.1 km), the method returns an empty Optional
     * to indicate the trip is complete. Otherwise, it returns a new position with an updated location
     * and a newly generated random speed.
     *
     * @param position The current position details of the car
     * @return An Optional containing the new position details, or empty if the car has reached its destination
     */
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

    /**
     * Generates a random speed value within the defined minimum and maximum limits.
     * @return A BigDecimal representing the randomly generated speed in kilometers per hour
     */
    private BigDecimal generateSpeedKmPerHour() {
        return new BigDecimal(MINIMUM_SPED_KM_H + random.nextInt(MAXIMUM_SPED_KM_H - MINIMUM_SPED_KM_H + 1));
    }
}
