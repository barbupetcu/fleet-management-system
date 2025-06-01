package com.barbu.fleetmanagement.penaltypoints.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
public class DriverPenaltyPoints {
    private Long driverId;
    private int totalPoints;
    private Instant lastUpdated;

    public DriverPenaltyPoints addPenaltyPoint(int points) {
        this.totalPoints += points;
        this.lastUpdated = Instant.now();
        return this;
    }
}