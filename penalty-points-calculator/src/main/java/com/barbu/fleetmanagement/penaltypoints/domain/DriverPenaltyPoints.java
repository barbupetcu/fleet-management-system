package com.barbu.fleetmanagement.penaltypoints.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents the accumulated penalty points for a driver.
 */
@Getter
@Setter
@NoArgsConstructor
public class DriverPenaltyPoints {
    private Long driverId;
    private int totalPoints;
    private Instant lastUpdated;

    /**
     * Adds penalty points to the driver's total.
     *
     * @param points the points to add
     * @return the updated DriverPenaltyPoints instance
     */
    public DriverPenaltyPoints addPoints(int points) {
        this.totalPoints += points;
        this.lastUpdated = Instant.now();
        return this;
    }
}