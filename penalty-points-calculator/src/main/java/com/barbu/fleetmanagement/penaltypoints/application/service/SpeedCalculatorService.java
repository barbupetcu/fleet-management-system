package com.barbu.fleetmanagement.penaltypoints.application.service;

import com.barbu.fleetmanagement.common.geo.GeoCalculator;
import com.barbu.fleetmanagement.common.model.CarPosition;
import com.barbu.fleetmanagement.penaltypoints.domain.CarSpeed;
import com.barbu.fleetmanagement.penaltypoints.domain.SpeedInterval;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Service for calculating car speed based on position data.
 * This service is responsible for:
 * <ul>
 *   <li>Calculating the speed of a car based on its current and previous positions</li>
 *   <li>Managing the car position cache to track vehicle movements</li>
 *   <li>Ensuring speed calculations are only performed when a car has moved at least 1 km</li>
 * </ul>
 * 
 * The service uses geographic calculations to determine distance traveled and
 * time-based calculations to determine speed in kilometers per hour.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SpeedCalculatorService {

    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal HOURS_IN_SECOND = new BigDecimal("0.000277778"); // 1/3600

    private final GeoCalculator geoCalculator;

    /**
     * Cache for storing the previous positions of cars.
     * Used to retrieve the last known position of a car for speed calculations.
     */
    private final CarPositionCache carPositionCache;

    /**
     * Calculates the speed of a car based on its current position and the previous position.
     * This method retrieves the previous position of the car from the cache and calculates
     * the speed based on the distance traveled and time elapsed. The calculation is only 
     * performed if the car has moved at least 1 kilometer since the last position update.
     * The method also updates the car position cache with the current position for future
     * speed calculations.
     *
     * @param currentPosition The current car position containing location, timestamp, and identifiers
     * @return An Optional CarSpeed object containing the calculated speed, or an empty Optional if:
     *         - This is the first position update for the trip (no previous position available)
     *         - The car has not moved at least 1 kilometer since the last position update
     */
    public Optional<CarSpeed> calculateCarSpeed(CarPosition currentPosition) {
        Long tripId = currentPosition.tripId();
        CarPosition previousPosition = carPositionCache.getByTripId(tripId);

        if (previousPosition == null) {
            // This is the first heartbeat of the trip, so save the current position in the cache
            carPositionCache.save(currentPosition);
            return Optional.empty();
        }

        // Calculate speed
        CarSpeed speed = calculateSpeed(currentPosition, previousPosition);
        // if the car has moved at least 1 km then return calculated speed
        if (speed.getDistanceKm().compareTo(BigDecimal.ONE) >= 0) {
            log.info("Calculated speed for car {}: {} km/h",
                    speed.getCarId(), speed.getSpeedKmh().doubleValue());
            carPositionCache.save(currentPosition);
            return Optional.of(speed);

        }

        // Otherwise wait until the next heartbeat to check if the car has moved at least 1 km
        return Optional.empty();
    }


    /**
     * Calculates the speed between two car positions.
     * This method performs the actual speed calculation by:
     * 1. Calculating the distance in kilometers between the two positions using the GeoCalculator
     * 2. Calculating the time difference in seconds between the position timestamps
     * 3. Converting the time difference to hours
     * 4. Calculating the speed in kilometers per hour (km/h)
     * The method also builds a comprehensive CarSpeed object containing all relevant information
     * about the car's movement, including identifiers, locations, timestamps, distance, and speed.
     *
     * @param current  The current car position containing location and timestamp
     * @param previous The previous car position containing location and timestamp
     * @return A CarSpeed object containing the calculated speed and all relevant movement data
     */
    private CarSpeed calculateSpeed(CarPosition current, CarPosition previous) {

        // Calculate distance in kilometers
        BigDecimal distanceKm = geoCalculator.calculateDistanceInKm(
                previous.currentLocation(),
                current.currentLocation());

        // Calculate the time difference in seconds
        Instant currentTime = current.timestamp();
        Instant previousTime = previous.timestamp();
        long secondsDifference = Duration.between(previousTime, currentTime).getSeconds();

        // Convert seconds to hours and calculate speed (km/h)
        BigDecimal hoursDifference = new BigDecimal(secondsDifference).multiply(HOURS_IN_SECOND);
        BigDecimal speedKmh = distanceKm.divide(hoursDifference, MATH_CONTEXT);

        return CarSpeed.builder()
                .carId(current.carId())
                .driverId(current.driverId())
                .tripId(current.tripId())
                .currentLocation(current.currentLocation())
                .previousLocation(previous.currentLocation())
                .currentTimestamp(current.timestamp())
                .previousTimestamp(previous.timestamp())
                .speedKmh(speedKmh)
                .distanceKm(distanceKm)
                .build();
    }
}
