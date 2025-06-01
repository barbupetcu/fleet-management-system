package com.barbu.fleetmanagement.simulator.api.producer;

import com.barbu.fleetmanagement.common.model.CarPosition;
import com.barbu.fleetmanagement.common.model.Location;
import com.barbu.fleetmanagement.simulator.domain.CarPositionDetails;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CarPositionProducerTest {

    @Mock
    Emitter<CarPosition> carPositionEmitter;

    @InjectMocks
    CarPositionProducer carPositionProducer;

    @Captor
    ArgumentCaptor<Message<CarPosition>> messageCaptor;

    private CarPositionDetails carPositionDetails;
    private final Long carId = 1L;
    private final Long driverId = 2L;
    private final Long tripId = 3L;
    private final Location currentLocation = new Location(new BigDecimal("40.7128"), new BigDecimal("-74.0060"));
    private final Location destination = new Location(new BigDecimal("34.0522"), new BigDecimal("-118.2437"));
    private final BigDecimal speedKmPerHour = new BigDecimal("60");
    private final Instant timestamp = Instant.now();

    @BeforeEach
    void setUp() {
        carPositionDetails = CarPositionDetails.builder()
                .carId(carId)
                .driverId(driverId)
                .tripId(tripId)
                .currentLocation(currentLocation)
                .destination(destination)
                .speedKmPerHour(speedKmPerHour)
                .timestamp(timestamp)
                .build();
    }

    @Test
    void sendCarPosition_shouldCreateCarPositionAndSendMessage() {
        carPositionProducer.sendCarPosition(carPositionDetails);

        verify(carPositionEmitter).send(messageCaptor.capture());
        Message<CarPosition> capturedMessage = messageCaptor.getValue();
        CarPosition carPosition = capturedMessage.getPayload();

        assertEquals(carId, carPosition.carId());
        assertEquals(driverId, carPosition.driverId());
        assertEquals(tripId, carPosition.tripId());
        assertEquals(currentLocation, carPosition.currentLocation());
        assertEquals(timestamp, carPosition.timestamp());

        OutgoingKafkaRecordMetadata<String> metadata = capturedMessage.getMetadata(OutgoingKafkaRecordMetadata.class).orElseThrow();
        assertEquals(driverId.toString(), metadata.getKey());

        Headers headers = metadata.getHeaders();
        Iterator<Header> iterator = headers.iterator();
        Header header = iterator.next();
        assertEquals("eventType", header.key());
        assertArrayEquals("position.updated".getBytes(), header.value());
    }
}
