package com.barbu.fleetmanagement.manager.application.mapper;

import com.barbu.fleetmanagement.common.model.Location;
import com.barbu.fleetmanagement.common.model.Trip;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TripMapper {

    public TripEntity to(Trip trip) {
        return TripEntity.builder()
                .latitudeStart(trip.start().latitude())
                .longitudeStart(trip.start().longitude())
                .latitudeDestination(trip.destination().latitude())
                .longitudeDestination(trip.destination().longitude())
                .driverId(trip.driverId())
                .carId(trip.carId())
                .build();
    }

    public Trip from(TripEntity entity) {
        return Trip.builder()
                .id(entity.getId())
                .start(new Location(entity.getLatitudeStart(), entity.getLongitudeStart()))
                .destination(new Location(entity.getLatitudeDestination(), entity.getLongitudeDestination()))
                .driverId(entity.getDriverId())
                .carId(entity.getCarId())
                .build();
    }
}
