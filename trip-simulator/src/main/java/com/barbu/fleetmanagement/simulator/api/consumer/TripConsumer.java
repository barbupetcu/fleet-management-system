package com.barbu.fleetmanagement.simulator.api.consumer;

import com.barbu.fleetmanagement.simulator.api.consumer.model.Trip;
import com.barbu.fleetmanagement.simulator.application.service.TripSimulatorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
public class TripConsumer {

    @Inject
    TripSimulatorService tripSimulatorService;
    
    @Incoming("trip")
    public void consumeTripEvent(Trip trip) {
        log.info("Received trip event for car {} with driver {}", trip.carId(), trip.driverId());
        tripSimulatorService.startTripSimulation(trip);
        log.info("Started trip simulation with ID {}", trip.id());
    }
}