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
 * <p>
 * This class defines a Kafka Streams processing topology that:
 * <ol>
 *   <li>Consumes car position events from a Kafka topic</li>
 *   <li>Calculates car speeds using the SpeedCalculatorService</li>
 *   <li>Filters out speeds that don't qualify for penalty points</li>
 *   <li>Assigns penalty points based on speed intervals</li>
 *   <li>Publishes individual penalty point events</li>
 *   <li>Aggregates penalty points by driver</li>
 *   <li>Publishes driver penalty point totals when they change</li>
 * </ol>
 * <p>
 * The topology maintains a state store to track the accumulated penalty points for each driver.
 */
@Slf4j
@ApplicationScoped
public class PenaltyPointsTopology {

    @Inject
    SpeedCalculatorService speedCalculatorService;

    /**
     * The name of the Kafka topic where driver penalty point totals are published.
     * This topic receives events when a driver's total penalty points change.
     */
    @ConfigProperty(name = "driver.penalty.points.topic")
    String driverPenaltyPointsTopic;

    /**
     * The name of the Kafka topic where individual penalty point events are published.
     * This topic receives an event each time penalty points are assigned.
     */
    @ConfigProperty(name = "penalty.points.topic")
    String penaltyPointsTopic;

    /**
     * Produces the Kafka Streams topology.
     * <p>
     * This method builds a Kafka Streams processing topology that:
     * <ol>
     *   <li>Creates a stream from the car position topic</li>
     *   <li>Calculates speeds for each car position using the SpeedCalculatorService</li>
     *   <li>Filters out positions where speed couldn't be calculated (car hasn't moved enough)</li>
     *   <li>Filters out speeds that don't fall within defined penalty intervals</li>
     *   <li>Maps speed data to penalty points based on the speed intervals</li>
     *   <li>Publishes individual penalty point events to the penalty points topic</li>
     *   <li>Groups penalty points by driver ID</li>
     *   <li>Aggregates penalty points for each driver in a state store</li>
     *   <li>Publishes driver penalty point totals to the driver penalty points topic</li>
     * </ol>
     *
     * @return A configured Kafka Streams Topology ready for execution
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

    /**
     * Creates a materialized state store configuration for storing driver penalty points.
     * <p>
     * This method configures a persistent key-value store that:
     * <ul>
     *   <li>Uses driver ID as the key (as a String)</li>
     *   <li>Stores DriverPenaltyPoints objects as values</li>
     * </ul>
     * The state store is used by the Kafka Streams aggregation operation to maintain
     * the running total of penalty points for each driver.
     *
     * @return A configured Materialized instance for the driver points state store
     */
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

    /**
     * Aggregates penalty points for a driver.
     * <p>
     * This method is used as the aggregator function in the Kafka Streams aggregation operation.
     * It performs the following:
     * <ol>
     *   <li>If this is the first penalty for a driver, initializes the driver ID</li>
     *   <li>Adds the new penalty points to the driver's running total</li>
     *   <li>Updates the last updated timestamp</li>
     * </ol>
     * The method is called by Kafka Streams for each new penalty point event for a driver,
     * allowing the system to maintain a running total of penalty points.
     *
     * @param driverId The ID of the driver (as a String, used as the key in the state store)
     * @param penaltyPoints The new penalty points to add
     * @param driverPenaltyPoints The current accumulated penalty points for the driver (or a new instance if none exist)
     * @return The updated DriverPenaltyPoints object with the new points added
     */
    private DriverPenaltyPoints aggregate(String driverId, PenaltyPoints penaltyPoints, DriverPenaltyPoints driverPenaltyPoints) {
        if (driverPenaltyPoints.getDriverId() == null) {
            driverPenaltyPoints.setDriverId(Long.parseLong(driverId));
        }
        return driverPenaltyPoints.addPenaltyPoint(penaltyPoints.penaltyPoints());
    }
}
