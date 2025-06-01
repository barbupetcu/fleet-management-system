package com.barbu.fleetmanagement.penaltypoints.application.service;

import com.barbu.fleetmanagement.common.geo.GeoCalculator;
import com.barbu.fleetmanagement.common.model.CarPosition;
import com.barbu.fleetmanagement.penaltypoints.domain.CarSpeed;
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
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SpeedCalculatorService {

    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal HOURS_IN_SECOND = new BigDecimal("0.000277778"); // 1/3600

    private final GeoCalculator geoCalculator;
    private final CarPositionCache carPositionCache;

    /**
     * Calculates the speed of a car based on its current position and the previous position.
     *
     * @param currentPosition The current car position
     * @return An Optional CarSpeed object containing the calculated speed, or an empty Optional if the car has not moved
     * at least 1 km
     */
    public Optional<CarSpeed> calculateCarSpeed(CarPosition currentPosition) {
        Long tripId = currentPosition.tripId();
        CarPosition previousPosition = carPositionCache.getByTripId(tripId);

        if (previousPosition == null) {
            //This is the first hearth beat of the trip, so save the current position in the cache
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

        //otherwise wait until the next hearth beat to check if the car has moved at least 1 km
        return Optional.empty();
    }


    /**
     * Calculates the speed between two car positions.
     *
     * @param current  The current car position
     * @param previous The previous car position
     * @return A CarSpeed object containing the calculated speed
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