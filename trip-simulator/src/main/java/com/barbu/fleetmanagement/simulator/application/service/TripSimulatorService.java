package com.barbu.fleetmanagement.simulator.application.service;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Trip;
import com.barbu.fleetmanagement.simulator.domain.CarPosition;
import com.barbu.fleetmanagement.simulator.domain.CarPositionRepository;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class TripSimulatorService {

    private static final int INTERVAL_SECONDS = 10;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Inject
    CarMover carMover;
    @Inject
    @Channel("hearth-beat")
    Emitter<CarPosition> carPositionEmitter;
    @Inject
    CarPositionRepository carPositionRepository;

    /**
     * Start simulating a new trip.
     * 
     * @param trip The trip to simulate
     */
    public void startTripSimulation(Trip trip) {
        CarPosition initialPosition = CarPosition.createInitialPosition(trip, trip.id());
        carPositionRepository.save(initialPosition);
        //TODO schedule position updates using a persisted scheduler(Quartz, Schedlock, etc)
        scheduler.schedule(() -> updatePosition(trip.id()), INTERVAL_SECONDS, TimeUnit.SECONDS);

        log.info("Started trip simulation {} for car {} with driver {} from {} to {}",
                trip.id(), trip.carId(), trip.driverId(),
                trip.start(), trip.destination());
    }

    private void updatePosition(Long tripId) {
        carPositionRepository.findByTripId(tripId)
                .flatMap(carPosition -> carMover.move(carPosition))
                .ifPresentOrElse(
                        this::scheduleNextPositionUpdate,
                        () -> {
                            carPositionRepository.deleteByTripId(tripId);
                            //TODO publish trip.completed event?
                        }
                );

    }

    private void scheduleNextPositionUpdate(CarPosition carPosition) {
        Headers headers = new RecordHeaders();
        headers.add("eventType", "position.updated".getBytes());
        OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String> builder()
                .withKey(carPosition.getDriverId().toString())
                .withHeaders(headers)
                .build();
        carPositionEmitter.send(Message.of(carPosition).addMetadata(metadata));
        carPositionRepository.save(carPosition);
        scheduler.schedule(() -> updatePosition(carPosition.getTripId()), INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
}
