package com.barbu.fleetmanagement.manager.application.service.panache;

import com.barbu.fleetmanagement.manager.api.exception.CarNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.DriverNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.TripNotFoundException;
import com.barbu.fleetmanagement.manager.api.model.Trip;
import com.barbu.fleetmanagement.manager.application.mapper.TripMapper;
import com.barbu.fleetmanagement.manager.application.service.TripService;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hibernate.exception.ConstraintViolationException;

@Transactional
@ApplicationScoped
public class PanacheTripService implements TripService {

    @Inject
    TripMapper tripMapper;
    @Inject
    @Channel("trip")
    Emitter<Trip> tripEmitter;

    @Override
    public Trip createTrip(Trip trip) {
        TripEntity tripEntity = tripMapper.to(trip);
        save(tripEntity);
        Trip savedTrip = tripMapper.from(tripEntity);
        OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String> builder()
                .withKey(savedTrip.driverId().toString())
                .build();
        tripEmitter.send(Message.of(savedTrip).addMetadata(metadata));
        return savedTrip;
    }

    private static void save(TripEntity tripEntity) {
        try {
            tripEntity.persistAndFlush();
        } catch (ConstraintViolationException e) {
            if (e.getMessage().contains("insert or update on table \"trip\" violates foreign key " +
                    "constraint \"fk_trip_car\"")) {
                throw new CarNotFoundException();
            } else if (e.getMessage().contains("insert or update on table \"trip\" violates foreign key " +
                    "constraint \"fk_trip_driver\"")) {
                throw new DriverNotFoundException();
            } else {
                throw e;
            }
        }
    }

    @Override
    public Trip findTrip(Long id) {
        return TripEntity.findByIdOptional(id)
                .map(TripEntity.class::cast)
                .map(tripMapper::from)
                .orElseThrow(TripNotFoundException::new);
    }
}