package com.barbu.fleetmanagement.simulator.api.consumer.model;

import lombok.Builder;

@Builder
public record Trip(Long id, Location start, Location destination, Long driverId, Long carId) {
}