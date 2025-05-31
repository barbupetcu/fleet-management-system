package com.barbu.fleetmanagement.manager.api.exception;

import jakarta.ws.rs.core.Response;

public class CarAlreadyExistsException extends ResponseStatusAwareException {
    private static final String MESSAGE = "Car already exists";

    public CarAlreadyExistsException() {
        super(Response.Status.CONFLICT, MESSAGE);
    }
}