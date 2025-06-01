package com.barbu.fleetmanagement.penaltypoints.stream;

import com.barbu.fleetmanagement.common.model.CarPosition;
import com.barbu.fleetmanagement.penaltypoints.domain.DriverPenaltyPoints;
import com.barbu.fleetmanagement.penaltypoints.domain.PenaltyPoints;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serde;

public class SerdesFactory {

    public static Serde<CarPosition> carPositionSerde() {
        return new ObjectMapperSerde<>(CarPosition.class);
    }

    public static Serde<PenaltyPoints> penaltyPointsSerde() {
        return new ObjectMapperSerde<>(PenaltyPoints.class);
    }

    public static Serde<DriverPenaltyPoints> driverPenaltyPointsSerde() {
        return new ObjectMapperSerde<>(DriverPenaltyPoints.class);
    }
}