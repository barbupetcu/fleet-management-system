package com.barbu.fleetmanagement.simulator.api.producer;

import com.barbu.fleetmanagement.common.model.CarPosition;
import com.barbu.fleetmanagement.simulator.domain.CarPositionDetails;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
public class CarPositionProducer {

    @Inject
    @Channel("hearth-beat")
    Emitter<CarPosition> carPositionEmitter;

    public void sendCarPosition(CarPositionDetails carPositionDetails) {
        CarPosition carPosition = CarPosition.builder()
                .carId(carPositionDetails.getCarId())
                .driverId(carPositionDetails.getDriverId())
                .tripId(carPositionDetails.getTripId())
                .currentLocation(carPositionDetails.getCurrentLocation())
                .timestamp(carPositionDetails.getTimestamp())
                .build();
        Headers headers = new RecordHeaders();
        headers.add("eventType", "position.updated".getBytes());
        OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String> builder()
                .withKey(carPositionDetails.getDriverId().toString())
                .withHeaders(headers)
                .build();
        carPositionEmitter.send(Message.of(carPosition).addMetadata(metadata));
    }
}
