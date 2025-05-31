package com.barbu.fleetmanagement.manager.api.resource;

import com.barbu.fleetmanagement.manager.api.model.Driver;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;
import com.barbu.fleetmanagement.manager.application.service.DriverService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.Set;

@Path("/drivers")
@RequiredArgsConstructor
public class DriverResource {

    @Inject
    DriverService panacheDriverService;

    @GET
    public PaginatedResponse<Driver> getDrivers(
            @QueryParam("page_size") int pageSize,
            @QueryParam("page") int page,
            @DefaultValue("id") @QueryParam("sort") Set<String> sort
    ) {
        return panacheDriverService.findDriver(pageSize, page, sort);
    }

    @PUT
    public Driver createDriver(@Valid Driver driver) {
        return panacheDriverService.createDriver(driver);
    }

    @GET
    @Path("/{id}")
    public Driver getDriver(@PathParam("id") Long id) {
        return panacheDriverService.findDriver(id);
    }

    @DELETE
    @Path("/{id}")
    @ResponseStatus(value = 204)
    public void deleteDriver(@PathParam("id") Long id) {
        panacheDriverService.deleteDriver(id);
    }

}
