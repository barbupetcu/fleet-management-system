package com.barbu.fleetmanagement.simulator.application.service;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Trip;
import com.barbu.fleetmanagement.simulator.api.producer.CarPositionProducer;
import com.barbu.fleetmanagement.simulator.domain.CarPositionDetails;
import com.barbu.fleetmanagement.simulator.domain.CarPositionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class TripSimulatorService {

    private static final int INTERVAL_SECONDS = 10;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private final CarMover carMover;
    private final CarPositionProducer carPositionProducer;
    private final CarPositionRepository carPositionRepository;

    /**
     * Start simulating a new trip.
     * 
     * @param trip The trip to simulate
     */
    public void startTripSimulation(Trip trip) {
        CarPositionDetails initialPosition = carMover.moveToInitialPosition(trip);
        carPositionRepository.save(initialPosition);
        //TODO schedule position updates using a persisted scheduler(Quartz, Schedlock, etc)
        scheduler.schedule(() -> updatePosition(trip.id()), INTERVAL_SECONDS, TimeUnit.SECONDS);

        log.info("Started trip simulation {} for car {} with driver {} from {} to {}",
                trip.id(), trip.carId(), trip.driverId(),
                trip.start(), trip.destination());
    }

    private void updatePosition(Long tripId) {
        carPositionRepository.findByTripId(tripId)
                .flatMap(carMover::move)
                .ifPresentOrElse(
                        this::scheduleNextPositionUpdate,
                        () -> {
                            carPositionRepository.deleteByTripId(tripId);
                            //TODO publish trip.completed event?
                        }
                );

    }

    private void scheduleNextPositionUpdate(CarPositionDetails carPositionDetails) {
        carPositionRepository.save(carPositionDetails);
        scheduler.schedule(() -> updatePosition(carPositionDetails.getTripId()), INTERVAL_SECONDS, TimeUnit.SECONDS);
        carPositionProducer.sendCarPosition(carPositionDetails);
    }
}
