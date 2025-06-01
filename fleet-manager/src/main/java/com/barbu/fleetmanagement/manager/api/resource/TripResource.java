package com.barbu.fleetmanagement.manager.api.resource;

import com.barbu.fleetmanagement.manager.api.model.Trip;
import com.barbu.fleetmanagement.manager.application.service.TripService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;

@Path("/trips")
@RequiredArgsConstructor
public class TripResource {

    @Inject
    TripService panacheTripService;

    @PUT
    public Trip createTrip(@Valid Trip trip) {
        return panacheTripService.createTrip(trip);
    }

    @GET
    @Path("/{id}")
    public Trip getTrip(@PathParam("id") Long id) {
        return panacheTripService.findTrip(id);
    }

    //TODO add delete trip endpoint which should emit an event trip.cancelled event which should stop the trip in trip-simulator service?

}