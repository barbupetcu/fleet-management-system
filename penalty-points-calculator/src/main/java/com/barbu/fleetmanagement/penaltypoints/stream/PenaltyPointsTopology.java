package com.barbu.fleetmanagement.penaltypoints.stream;

import com.barbu.fleetmanagement.common.model.CarPosition;
import com.barbu.fleetmanagement.penaltypoints.application.service.SpeedCalculatorService;
import com.barbu.fleetmanagement.penaltypoints.domain.CarSpeed;
import com.barbu.fleetmanagement.penaltypoints.domain.DriverPenaltyPoints;
import com.barbu.fleetmanagement.penaltypoints.domain.PenaltyPoints;
import com.barbu.fleetmanagement.penaltypoints.domain.SpeedInterval;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

/**
 * Kafka Streams topology for calculating penalty points based on car speeds.
 */
@Slf4j
@ApplicationScoped
public class PenaltyPointsTopology {


    @Inject
    SpeedCalculatorService speedCalculatorService;

    @ConfigProperty(name = "driver.penalty.points.topic")
    String driverPenaltyPointsTopic;
    @ConfigProperty(name = "penalty.points.topic")
    String penaltyPointsTopic;

    /**
     * Produces the Kafka Streams topology.
     */
    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // Create a stream of car positions from the input topic
        KStream<String, CarPosition> carPositions = builder.stream(
                "fleet-management.car.position",
                Consumed.with(Serdes.String(), SerdesFactory.carPositionSerde())
        );

        // Calculate speed for each car position if the car has moved at least 1 km
        carPositions
                .mapValues(carPosition -> speedCalculatorService.calculateCarSpeed(carPosition))
                .filter((_, speed) -> speed.isPresent())
                .mapValues(Optional::get)
                .filter((_, speed) -> SpeedInterval.getInterval(speed.getSpeedKmh().doubleValue()).isPresent())
                .mapValues((_, carSpeed) -> mapToPenaltyPoints(carSpeed))
                //publish an event for each penalty 
                .repartition(Repartitioned.with(Serdes.String(), SerdesFactory.penaltyPointsSerde()).withName(penaltyPointsTopic))
                .groupByKey(Grouped.with(Serdes.String(), SerdesFactory.penaltyPointsSerde()))
                .aggregate(
                        DriverPenaltyPoints::new,
                        this::aggregate,
                        getDriverPointsStore()
                )
                .toStream()
                //publish events when penalty points of the driver have changed
                .to(driverPenaltyPointsTopic, Produced.with(Serdes.String(), SerdesFactory.driverPenaltyPointsSerde()));

        return builder.build();
    }

    private static Materialized<String, DriverPenaltyPoints, KeyValueStore<Bytes, byte[]>> getDriverPointsStore() {
        return Materialized.<String, DriverPenaltyPoints, KeyValueStore<Bytes, byte[]>>as("driver-points-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(SerdesFactory.driverPenaltyPointsSerde());
    }

    private static PenaltyPoints mapToPenaltyPoints(CarSpeed carSpeed) {
        return PenaltyPoints.builder()
                .driverId(carSpeed.getDriverId())
                .carId(carSpeed.getCarId())
                .tripId(carSpeed.getTripId())
                .penaltyTime(carSpeed.getCurrentTimestamp())
                .penaltyPoints(
                        SpeedInterval.getInterval(carSpeed.getSpeedKmh().doubleValue())
                                .map(SpeedInterval::getPenaltyPointsPerKm)
                                .orElse(0)
                )
                .build();
    }

    private DriverPenaltyPoints aggregate(String driverId, PenaltyPoints penaltyPoints, DriverPenaltyPoints driverPenaltyPoints) {
        if (driverPenaltyPoints.getDriverId() == null) {
            driverPenaltyPoints.setDriverId(Long.parseLong(driverId));
        }
        return driverPenaltyPoints.addPenaltyPoint(penaltyPoints.penaltyPoints());
    }
}