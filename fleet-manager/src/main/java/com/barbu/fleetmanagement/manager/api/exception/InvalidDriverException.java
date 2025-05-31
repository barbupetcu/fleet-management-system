package com.barbu.fleetmanagement.manager.api.exception;

import jakarta.ws.rs.core.Response;

public class InvalidDriverException extends ResponseStatusAwareException {

    public InvalidDriverException(String message) {
        super(Response.Status.BAD_REQUEST, message);
    }
}
