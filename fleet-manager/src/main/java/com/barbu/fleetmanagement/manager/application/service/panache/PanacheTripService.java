package com.barbu.fleetmanagement.manager.application.service.panache;

import com.barbu.fleetmanagement.manager.api.exception.CarNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.DriverNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.TripNotFoundException;
import com.barbu.fleetmanagement.manager.api.model.Trip;
import com.barbu.fleetmanagement.manager.application.mapper.TripMapper;
import com.barbu.fleetmanagement.manager.application.service.TripService;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class PanacheTripService implements TripService {

    private final TripMapper tripMapper;

    @Override
    public Trip createTrip(Trip trip) {
        TripEntity tripEntity = tripMapper.to(trip);
        save(tripEntity);
        return tripMapper.from(tripEntity);
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