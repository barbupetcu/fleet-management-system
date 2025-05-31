package com.barbu.fleetmanagement.manager.api.resource;

import com.barbu.fleetmanagement.manager.api.model.Car;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;
import com.barbu.fleetmanagement.manager.application.service.CarService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.Set;

@Path("/cars")
@RequiredArgsConstructor
public class CarResource {

    @Inject
    CarService panacheCarService;

    @GET
    public PaginatedResponse<Car> getCars(
            @QueryParam("page_size") int pageSize,
            @QueryParam("page") int page,
            @DefaultValue("id") @QueryParam("sort") Set<String> sort
    ) {
        return panacheCarService.findCar(pageSize, page, sort);
    }

    @PUT
    public Car createCar(@Valid Car car) {
        return panacheCarService.createCar(car);
    }

    @GET
    @Path("/{id}")
    public Car getCar(@PathParam("id") Long id) {
        return panacheCarService.findCar(id);
    }

    @DELETE
    @Path("/{id}")
    @ResponseStatus(value = 204)
    public void deleteCar(@PathParam("id") Long id) {
        panacheCarService.deleteCar(id);
    }
}