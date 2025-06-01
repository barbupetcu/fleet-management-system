package com.barbu.fleetmanagement.manager.application.mapper;

import com.barbu.fleetmanagement.common.model.Location;
import com.barbu.fleetmanagement.common.model.Trip;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TripMapperTest {

    private final TripMapper tripMapper = new TripMapper();

    @Test
    void toTripEntity() {
        BigDecimal startLatitude = new BigDecimal("40.7128");
        BigDecimal startLongitude = new BigDecimal("-74.0060");
        BigDecimal destLatitude = new BigDecimal("34.0522");
        BigDecimal destLongitude = new BigDecimal("-118.2437");
        Long driverId = 1L;
        Long carId = 2L;

        Location start = new Location(startLatitude, startLongitude);
        Location destination = new Location(destLatitude, destLongitude);

        Trip trip = Trip.builder()
                .start(start)
                .destination(destination)
                .driverId(driverId)
                .carId(carId)
                .build();

        TripEntity tripEntity = tripMapper.to(trip);

        assertThat(tripEntity)
                .returns(startLatitude, TripEntity::getLatitudeStart)
                .returns(startLongitude, TripEntity::getLongitudeStart)
                .returns(destLatitude, TripEntity::getLatitudeDestination)
                .returns(destLongitude, TripEntity::getLongitudeDestination)
                .returns(driverId, TripEntity::getDriverId)
                .returns(carId, TripEntity::getCarId);
    }

    @Test
    void fromTripEntity() {
        BigDecimal startLatitude = new BigDecimal("40.7128");
        BigDecimal startLongitude = new BigDecimal("-74.0060");
        BigDecimal destLatitude = new BigDecimal("34.0522");
        BigDecimal destLongitude = new BigDecimal("-118.2437");
        Long driverId = 1L;
        Long carId = 2L;
        Long id = 3L;

        TripEntity tripEntity = TripEntity.builder()
                .id(id)
                .latitudeStart(startLatitude)
                .longitudeStart(startLongitude)
                .latitudeDestination(destLatitude)
                .longitudeDestination(destLongitude)
                .driverId(driverId)
                .carId(carId)
                .build();

        Trip trip = tripMapper.from(tripEntity);

        assertThat(trip)
                .returns(id, Trip::id)
                .returns(driverId, Trip::driverId)
                .returns(carId, Trip::carId);

        assertThat(trip.start())
                .returns(startLatitude, Location::latitude)
                .returns(startLongitude, Location::longitude);

        assertThat(trip.destination())
                .returns(destLatitude, Location::latitude)
                .returns(destLongitude, Location::longitude);
    }
}
