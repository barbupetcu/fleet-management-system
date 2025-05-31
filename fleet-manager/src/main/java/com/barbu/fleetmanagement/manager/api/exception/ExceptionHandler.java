package com.barbu.fleetmanagement.manager.api.exception;

import com.barbu.fleetmanagement.manager.api.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        log.error("Received exception after calling Rest endpoint", exception);
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), LocalDateTime.now());
        if (exception instanceof ResponseStatusAwareException responseStatusAwareException) {
            return Response.status(responseStatusAwareException.getStatus())
                    .entity(errorResponse)
                    .build();
        }
        return Response.serverError()
                .entity(errorResponse)
                .build();
    }
}
