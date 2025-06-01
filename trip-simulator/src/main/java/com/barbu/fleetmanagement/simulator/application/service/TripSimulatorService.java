package com.barbu.fleetmanagement.simulator.application.service;

import com.barbu.fleetmanagement.common.model.Trip;
import com.barbu.fleetmanagement.simulator.api.producer.CarPositionProducer;
import com.barbu.fleetmanagement.simulator.domain.CarPositionDetails;
import com.barbu.fleetmanagement.simulator.domain.CarPositionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for simulating trips by scheduling and managing car position updates.
 * <p>
 * This class handles the lifecycle of trip simulations, including:
 * <ul>
 *   <li>Starting new trip simulations</li>
 *   <li>Scheduling periodic position updates</li>
 *   <li>Managing the completion of trips</li>
 * </ul>
 * The service uses a scheduler to periodically update car positions during a trip simulation
 * and publishes these position updates to a Kafka topic through the CarPositionProducer.
 * <p>
 * Position data is persisted in a repository to maintain state between updates.
 */
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
     * <p>
     * This method initializes a new trip simulation by:
     * <ol>
     *   <li>Creating an initial position for the car at the trip's starting point</li>
     *   <li>Persisting this position in the repository</li>
     *   <li>Scheduling the first position update after the defined interval</li>
     * </ol>
     * The simulation will continue automatically through scheduled updates until
     * the car reaches its destination.
     * 
     * @param trip The trip to simulate, containing car ID, driver ID, start and destination locations
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

    /**
     * Updates the position of a car for a specific trip.
     * <p>
     * This method:
     * <ol>
     *   <li>Retrieves the current position for the trip from the repository</li>
     *   <li>Uses the CarMover to calculate the next position</li>
     *   <li>If a new position is returned (car hasn't reached destination):
     *     <ul>
     *       <li>Schedules the next position update</li>
     *     </ul>
     *   </li>
     *   <li>If no new position is returned (car has reached destination):
     *     <ul>
     *       <li>Cleans up by removing the trip data from the repository</li>
     *       <li>Could potentially publish a trip completion event (TODO)</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param tripId The ID of the trip to update
     */
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

    /**
     * Schedules the next position update for a trip and publishes the current position.
     * <p>
     * This method:
     * <ol>
     *   <li>Persists the updated car position in the repository</li>
     *   <li>Schedules the next position update after the defined interval</li>
     *   <li>Publishes the current position to Kafka via the CarPositionProducer</li>
     * </ol>
     * This method is called when a car has not yet reached its destination and
     * needs to continue the simulation.
     *
     * @param carPositionDetails The updated position details of the car
     */
    private void scheduleNextPositionUpdate(CarPositionDetails carPositionDetails) {
        carPositionRepository.save(carPositionDetails);
        scheduler.schedule(() -> updatePosition(carPositionDetails.getTripId()), INTERVAL_SECONDS, TimeUnit.SECONDS);
        carPositionProducer.sendCarPosition(carPositionDetails);
    }
}
