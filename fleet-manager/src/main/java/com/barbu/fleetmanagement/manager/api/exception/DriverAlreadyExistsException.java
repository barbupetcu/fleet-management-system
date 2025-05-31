package com.barbu.fleetmanagement.manager.api.exception;

import jakarta.ws.rs.core.Response;

public class DriverAlreadyExistsException extends ResponseStatusAwareException {
    private static final String MESSAGE = "Driver already exists";

    public DriverAlreadyExistsException() {
        super(Response.Status.CONFLICT, MESSAGE);
    }
}
