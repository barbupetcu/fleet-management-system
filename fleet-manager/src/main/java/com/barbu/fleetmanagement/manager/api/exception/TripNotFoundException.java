package com.barbu.fleetmanagement.manager.api.exception;

import jakarta.ws.rs.core.Response;

public class TripNotFoundException extends ResponseStatusAwareException {
    private static final String MESSAGE = "Trip not found";

    public TripNotFoundException() {
        super(Response.Status.NOT_FOUND, MESSAGE);
    }
}