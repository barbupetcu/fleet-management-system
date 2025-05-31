package com.barbu.fleetmanagement.manager.api.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class ResponseStatusAwareException extends RuntimeException {

    private final Response.Status status;

    public ResponseStatusAwareException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }
}
