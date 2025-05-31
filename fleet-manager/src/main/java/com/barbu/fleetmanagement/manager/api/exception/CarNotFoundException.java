package com.barbu.fleetmanagement.manager.api.exception;

import jakarta.ws.rs.core.Response;

public class CarNotFoundException extends ResponseStatusAwareException {
    private static final String MESSAGE = "Car not found";

    public CarNotFoundException() {
        super(Response.Status.NOT_FOUND, MESSAGE);
    }
}