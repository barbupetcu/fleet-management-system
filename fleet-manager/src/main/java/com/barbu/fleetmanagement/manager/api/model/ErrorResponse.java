package com.barbu.fleetmanagement.manager.api.model;

import java.time.LocalDateTime;

public record ErrorResponse(String message, LocalDateTime timestamp) {
}
