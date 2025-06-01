package com.barbu.fleetmanagement.common.geo;

import com.barbu.fleetmanagement.common.model.Location;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class GeoCalculator {
    private static final int EARTH_RADIUS_KM = 6371;
    public static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * Calculate the distance between two locations in kilometers using the Haversine formula.
     */
    public BigDecimal calculateDistanceInKm(Location start, Location destination) {
        BigDecimal lat1 = toRadians(start.latitude());
        BigDecimal lon1 = toRadians(start.longitude());
        BigDecimal lat2 = toRadians(destination.latitude());
        BigDecimal lon2 = toRadians(destination.longitude());

        BigDecimal dLat = lat2.subtract(lat1);
        BigDecimal dLon = lon2.subtract(lon1);

        BigDecimal a = sin(dLat.divide(new BigDecimal("2"), MATH_CONTEXT)).pow(2)
                .add(cos(lat1).multiply(cos(lat2))
                        .multiply(sin(dLon.divide(new BigDecimal("2"), MATH_CONTEXT)).pow(2)));

        BigDecimal c = new BigDecimal("2").multiply(atan2(sqrt(a), sqrt(BigDecimal.ONE.subtract(a))));

        return new BigDecimal(EARTH_RADIUS_KM).multiply(c);
    }

    /**
     * Calculate a new location by moving from start towards destination by the specified distance in kilometers.
     */
    public Location calculateNewPosition(Location start, Location destination, BigDecimal distanceToMoveKm) {
        BigDecimal totalDistanceKm = calculateDistanceInKm(start, destination);

        // If the care is very close to the destination, just return the destination
        if (totalDistanceKm.compareTo(new BigDecimal("0.1")) < 0) {
            return destination;
        }

        // If the distance to move is greater than the total distance, return the destination
        if (distanceToMoveKm.compareTo(totalDistanceKm) >= 0) {
            return destination;
        }

        // Calculate the fraction of the total distance to move
        BigDecimal fraction = distanceToMoveKm.divide(totalDistanceKm, MATH_CONTEXT);

        // Interpolate between start and destination
        BigDecimal newLat = start.latitude().add(
                destination.latitude().subtract(start.latitude()).multiply(fraction), MATH_CONTEXT);
        BigDecimal newLon = start.longitude().add(
                destination.longitude().subtract(start.longitude()).multiply(fraction), MATH_CONTEXT);

        return new Location(newLat, newLon);
    }

    private static BigDecimal toRadians(BigDecimal degrees) {
        return degrees.multiply(new BigDecimal(Math.PI)).divide(new BigDecimal("180"), MATH_CONTEXT);
    }

    private static BigDecimal sin(BigDecimal x) {
        return new BigDecimal(Math.sin(x.doubleValue()), MATH_CONTEXT);
    }

    private static BigDecimal cos(BigDecimal x) {
        return new BigDecimal(Math.cos(x.doubleValue()), MATH_CONTEXT);
    }

    private static BigDecimal sqrt(BigDecimal x) {
        return new BigDecimal(Math.sqrt(x.doubleValue()), MATH_CONTEXT);
    }

    private static BigDecimal atan2(BigDecimal y, BigDecimal x) {
        return new BigDecimal(Math.atan2(y.doubleValue(), x.doubleValue()), MATH_CONTEXT);
    }
}
