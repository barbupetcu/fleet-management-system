package com.barbu.fleetmanagement.manager.application.service;

import com.barbu.fleetmanagement.common.model.Trip;

public interface TripService {

    Trip createTrip(Trip trip);

    Trip findTrip(Long id);
}