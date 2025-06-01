package com.barbu.fleetmanagement.penaltypoints.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum SpeedInterval {
    SLOW(60, 79, 2),
    FAST(80, Double.MAX_VALUE, 5);

    private final double lowerLimit;
    private final double upperLimit;
    private final int penaltyPointsPerKm;

    public static Optional<SpeedInterval> getInterval(double speedKmPerHour) {
        return Arrays.stream(values())
                .filter(interval -> speedKmPerHour >= interval.getLowerLimit() && speedKmPerHour <= interval.getUpperLimit())
                .findFirst();
    }

}
