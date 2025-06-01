package com.barbu.fleetmanagement.simulator.application.service;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Location;
import com.barbu.fleetmanagement.simulator.api.consumer.model.Trip;
import com.barbu.fleetmanagement.simulator.domain.CarPositionDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarMoverTest {

    @Mock
    private GeoCalculator geoCalculator;

    @InjectMocks
    private CarMover carMover;

    private Trip trip;
    private CarPositionDetails carPositionDetails;
    private Location startLocation;
    private Location destinationLocation;
    private Location intermediateLocation;

    @BeforeEach
    void setUp() {
        startLocation = new Location(new BigDecimal("40.7128"), new BigDecimal("-74.0060")); // New York
        destinationLocation = new Location(new BigDecimal("34.0522"), new BigDecimal("-118.2437")); // Los Angeles
        intermediateLocation = new Location(new BigDecimal("37.7749"), new BigDecimal("-96.1252")); // Somewhere in between

        trip = Trip.builder()
                .id(1L)
                .carId(2L)
                .driverId(3L)
                .start(startLocation)
                .destination(destinationLocation)
                .build();

        carPositionDetails = CarPositionDetails.builder()
                .carId(trip.carId())
                .driverId(trip.driverId())
                .tripId(trip.id())
                .currentLocation(startLocation)
                .destination(destinationLocation)
                .speedKmPerHour(new BigDecimal("60"))
                .timestamp(Instant.now().minusSeconds(60)) // 1 minute ago
                .build();
    }

    @Nested
    class MoveToInitialPosition {
        @Test
        void shouldCreateCarPositionDetailsWithInitialPosition() {
            CarPositionDetails result = carMover.moveToInitialPosition(trip);

            assertNotNull(result);
            assertEquals(trip.carId(), result.getCarId());
            assertEquals(trip.driverId(), result.getDriverId());
            assertEquals(trip.id(), result.getTripId());
            assertEquals(trip.start(), result.getCurrentLocation());
            assertEquals(trip.destination(), result.getDestination());
            assertNotNull(result.getSpeedKmPerHour());
            assertNotNull(result.getTimestamp());
        }
    }

    @Nested
    class Move {
        @Test
        void move_shouldCalculateNewPositionAndReturnUpdatedCarPositionDetails() {
            BigDecimal remainingDistance = new BigDecimal("100.0"); // 100 km

            when(geoCalculator.calculateNewPosition(any(), any(), any())).thenReturn(intermediateLocation);
            when(geoCalculator.calculateDistanceInKm(intermediateLocation, destinationLocation)).thenReturn(remainingDistance);

            Optional<CarPositionDetails> result = carMover.move(carPositionDetails);

            assertTrue(result.isPresent());
            CarPositionDetails updatedPosition = result.get();
            assertEquals(carPositionDetails.getCarId(), updatedPosition.getCarId());
            assertEquals(carPositionDetails.getDriverId(), updatedPosition.getDriverId());
            assertEquals(carPositionDetails.getTripId(), updatedPosition.getTripId());
            assertEquals(intermediateLocation, updatedPosition.getCurrentLocation());
            assertEquals(destinationLocation, updatedPosition.getDestination());
            assertNotNull(updatedPosition.getSpeedKmPerHour());
            assertNotNull(updatedPosition.getTimestamp());
            assertTrue(updatedPosition.getTimestamp().isAfter(carPositionDetails.getTimestamp()));

            verify(geoCalculator).calculateNewPosition(eq(startLocation), eq(destinationLocation), any(BigDecimal.class));
            verify(geoCalculator).calculateDistanceInKm(intermediateLocation, destinationLocation);
        }

        @Test
        void move_shouldReturnEmptyOptionalWhenDestinationIsReached() {
            BigDecimal remainingDistance = new BigDecimal("0.05"); // Less than 0.1 km

            when(geoCalculator.calculateNewPosition(any(), any(), any())).thenReturn(intermediateLocation);
            when(geoCalculator.calculateDistanceInKm(intermediateLocation, destinationLocation)).thenReturn(remainingDistance);

            Optional<CarPositionDetails> result = carMover.move(carPositionDetails);

            assertFalse(result.isPresent());

            verify(geoCalculator).calculateNewPosition(eq(startLocation), eq(destinationLocation), any(BigDecimal.class));
            verify(geoCalculator).calculateDistanceInKm(intermediateLocation, destinationLocation);
        }

        @Test
        void move_shouldHandleZeroElapsedTime() {
            CarPositionDetails currentPosition = CarPositionDetails.builder()
                    .carId(trip.carId())
                    .driverId(trip.driverId())
                    .tripId(trip.id())
                    .currentLocation(startLocation)
                    .destination(destinationLocation)
                    .speedKmPerHour(new BigDecimal("60"))
                    .timestamp(Instant.now())
                    .build();

            BigDecimal remainingDistance = new BigDecimal("100.0"); // 100 km

            when(geoCalculator.calculateNewPosition(any(), any(), any())).thenReturn(startLocation); // No movement due to zero elapsed time
            when(geoCalculator.calculateDistanceInKm(startLocation, destinationLocation)).thenReturn(remainingDistance);

            Optional<CarPositionDetails> result = carMover.move(currentPosition);

            assertTrue(result.isPresent());
            CarPositionDetails updatedPosition = result.get();
            assertEquals(currentPosition.getCarId(), updatedPosition.getCarId());
            assertEquals(currentPosition.getDriverId(), updatedPosition.getDriverId());
            assertEquals(currentPosition.getTripId(), updatedPosition.getTripId());
            assertEquals(startLocation, updatedPosition.getCurrentLocation());
            assertEquals(destinationLocation, updatedPosition.getDestination());
            assertNotNull(updatedPosition.getSpeedKmPerHour());
            assertNotNull(updatedPosition.getTimestamp());
            assertTrue(updatedPosition.getTimestamp().compareTo(currentPosition.getTimestamp()) >= 0);

            verify(geoCalculator).calculateNewPosition(eq(startLocation), eq(destinationLocation), any(BigDecimal.class));
            verify(geoCalculator).calculateDistanceInKm(startLocation, destinationLocation);
        }
    }
}