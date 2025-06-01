package com.barbu.fleetmanagement.simulator.application.service;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Location;
import com.barbu.fleetmanagement.simulator.api.consumer.model.Trip;
import com.barbu.fleetmanagement.simulator.api.producer.CarPositionProducer;
import com.barbu.fleetmanagement.simulator.domain.CarPositionDetails;
import com.barbu.fleetmanagement.simulator.domain.CarPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripSimulatorServiceTest {

    @Mock
    private CarMover carMover;

    @Mock
    private CarPositionProducer carPositionProducer;

    @Mock
    private CarPositionRepository carPositionRepository;

    @Mock
    private ScheduledExecutorService scheduler;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @InjectMocks
    private TripSimulatorService tripSimulatorService;

    private Trip trip;
    private CarPositionDetails initialPosition;
    private CarPositionDetails updatedPosition;

    @BeforeEach
    void setUp() {
        // Setup test data
        Location startLocation = new Location(new BigDecimal("40.7128"), new BigDecimal("-74.0060")); // New York
        Location destinationLocation = new Location(new BigDecimal("34.0522"), new BigDecimal("-118.2437")); // Los Angeles

        trip = Trip.builder()
                .id(1L)
                .carId(2L)
                .driverId(3L)
                .start(startLocation)
                .destination(destinationLocation)
                .build();

        initialPosition = CarPositionDetails.builder()
                .carId(trip.carId())
                .driverId(trip.driverId())
                .tripId(trip.id())
                .currentLocation(startLocation)
                .destination(destinationLocation)
                .speedKmPerHour(new BigDecimal("60"))
                .timestamp(Instant.now())
                .build();

        updatedPosition = CarPositionDetails.builder()
                .carId(trip.carId())
                .driverId(trip.driverId())
                .tripId(trip.id())
                .currentLocation(new Location(new BigDecimal("37.7749"), new BigDecimal("-96.1252"))) // Somewhere in between
                .destination(destinationLocation)
                .speedKmPerHour(new BigDecimal("70"))
                .timestamp(Instant.now().plusSeconds(600)) // 10 minutes later
                .build();

        // Replace the scheduler in TripSimulatorService with our mock
        try {
            java.lang.reflect.Field schedulerField = TripSimulatorService.class.getDeclaredField("scheduler");
            schedulerField.setAccessible(true);
            schedulerField.set(tripSimulatorService, scheduler);
        } catch (Exception e) {
            fail("Failed to replace scheduler: " + e.getMessage());
        }
    }

    @Nested
    class StartTripSimulation {
        @Test
        void shouldInitializePositionAndScheduleUpdate() {
            when(carMover.moveToInitialPosition(trip)).thenReturn(initialPosition);

            tripSimulatorService.startTripSimulation(trip);

            verify(carMover).moveToInitialPosition(trip);
            verify(carPositionRepository).save(initialPosition);
            verify(scheduler).schedule(runnableCaptor.capture(), eq(10L), eq(TimeUnit.SECONDS));

            Runnable scheduledTask = runnableCaptor.getValue();
            assertNotNull(scheduledTask);
        }
    }

    @Nested
    class UpdatePosition {
        @Test
        void shouldMoveCarAndScheduleNextUpdateWhenNotAtDestination() {
            when(carPositionRepository.findByTripId(trip.id())).thenReturn(Optional.of(initialPosition));
            when(carMover.move(initialPosition)).thenReturn(Optional.of(updatedPosition));

            tripSimulatorService.startTripSimulation(trip);
            verify(scheduler).schedule(runnableCaptor.capture(), eq(10L), eq(TimeUnit.SECONDS));
            Runnable updatePositionTask = runnableCaptor.getValue();
            updatePositionTask.run();

            verify(carPositionRepository).findByTripId(trip.id());
            verify(carMover).move(initialPosition);
            verify(carPositionRepository).save(updatedPosition);
            verify(carPositionProducer).sendCarPosition(updatedPosition);
            verify(scheduler, times(2)).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS));
        }

        @Test
        void shouldDeleteTripWhenDestinationIsReached() {
            when(carPositionRepository.findByTripId(trip.id())).thenReturn(Optional.of(initialPosition));
            when(carMover.move(initialPosition)).thenReturn(Optional.empty()); // Destination reached

            tripSimulatorService.startTripSimulation(trip);
            verify(scheduler).schedule(runnableCaptor.capture(), eq(10L), eq(TimeUnit.SECONDS));
            Runnable updatePositionTask = runnableCaptor.getValue();
            updatePositionTask.run();

            verify(carPositionRepository).findByTripId(trip.id());
            verify(carMover).move(initialPosition);
            verify(carPositionRepository).deleteByTripId(trip.id());
            verify(carPositionRepository, never()).save(any(CarPositionDetails.class));
            verify(carPositionProducer, never()).sendCarPosition(any(CarPositionDetails.class));
            verify(scheduler, times(1)).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS));
        }
    }
}
