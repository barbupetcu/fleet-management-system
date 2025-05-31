package com.barbu.fleetmanagement.manager.api.exception;

import jakarta.ws.rs.core.Response;

public class DriverNotFoundException extends ResponseStatusAwareException {
    private static final String MESSAGE = "Driver not found";

    public DriverNotFoundException() {
        super(Response.Status.NOT_FOUND, MESSAGE);
    }
}
