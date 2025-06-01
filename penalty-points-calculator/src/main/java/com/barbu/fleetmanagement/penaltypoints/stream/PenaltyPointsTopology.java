package com.barbu.fleetmanagement.penaltypoints.stream;

import com.barbu.fleetmanagement.common.model.CarPosition;
import com.barbu.fleetmanagement.penaltypoints.application.service.SpeedCalculatorService;
import com.barbu.fleetmanagement.penaltypoints.domain.CarSpeed;
import com.barbu.fleetmanagement.penaltypoints.domain.DriverPenaltyPoints;
import com.barbu.fleetmanagement.penaltypoints.domain.SpeedInterval;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Optional;

/**
 * Kafka Streams topology for calculating penalty points based on car speeds.
 */
@Slf4j
@ApplicationScoped
public class PenaltyPointsTopology {


    @Inject
    SpeedCalculatorService speedCalculatorService;

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
        KStream<String, CarSpeed> carSpeeds = carPositions
                .mapValues(carPosition -> speedCalculatorService.calculateCarSpeed(carPosition))
                .filter((key, speed) -> speed.isPresent())
                .mapValues(Optional::get);

        // Calculate penalty points based on speed
        KStream<String, Integer> penaltyPoints = carSpeeds
                .mapValues(speed -> SpeedInterval.getInterval(speed.getSpeedKmh().doubleValue()))
                .filter((key, speedInterval) -> speedInterval.isPresent())
                .mapValues(Optional::get)
                .mapValues(SpeedInterval::getPenaltyPointsPerKm);

        // Change key from trip ID to driver ID for accumulating points by driver
        // Create a pair of driver ID and points
        joinCardSpeedsWithPenaltyPoints(carSpeeds, penaltyPoints);

        return builder.build();
    }

    private void joinCardSpeedsWithPenaltyPoints(KStream<String, CarSpeed> carSpeeds, KStream<String, Integer> penaltyPoints) {
        carSpeeds
                .join(
                        penaltyPoints,
                        DriverPoints::new,
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMillis(100))
                )
                .selectKey((key, driverPoints) -> driverPoints.driverId)
                .mapValues(driverPoints -> driverPoints.points)
                .groupByKey(
                        Grouped.with(Serdes.String(), Serdes.Integer())
                )
                .aggregate(
                        DriverPenaltyPoints::new,
                        (key, points, aggregate) -> {
                            if (aggregate.getDriverId() == null) {
                                aggregate.setDriverId(Long.valueOf(key));
                            }
                            return aggregate.addPoints(points);
                        },
                        Materialized.<String, DriverPenaltyPoints, KeyValueStore<Bytes, byte[]>>as("driver-points-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdesFactory.driverPenaltyPointsSerde())
                )
                .toStream()
                .to(penaltyPointsTopic, Produced.with(Serdes.String(), SerdesFactory.driverPenaltyPointsSerde()));
    }

    @Getter
    private static class DriverPoints {
        private final String driverId;
        private final int points;

        private DriverPoints(CarSpeed carSpeed, Integer points) {
            this.driverId = carSpeed.getDriverId().toString();
            this.points = points;
        }
    }
}