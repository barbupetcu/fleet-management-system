package com.barbu.fleetmanagement.manager.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Trip extends PanacheEntity {

  private double latitudeStart;
  private double longitudeStart;
  private double latitudeDestination;
  private double longitudeDestination;
  private Long driverId;
  private Long carId;

  public double getLatitudeStart() {
    return latitudeStart;
  }

  public void setLatitudeStart(double latitudeStart) {
    this.latitudeStart = latitudeStart;
  }

  public double getLongitudeStart() {
    return longitudeStart;
  }

  public void setLongitudeStart(double longitudeStart) {
    this.longitudeStart = longitudeStart;
  }

  public double getLatitudeDestination() {
    return latitudeDestination;
  }

  public void setLatitudeDestination(double latitudeDestination) {
    this.latitudeDestination = latitudeDestination;
  }

  public double getLongitudeDestination() {
    return longitudeDestination;
  }

  public void setLongitudeDestination(double longitudeDestination) {
    this.longitudeDestination = longitudeDestination;
  }

  public Long getDriverId() {
    return driverId;
  }

  public void setDriverId(Long driverId) {
    this.driverId = driverId;
  }

  public Long getCarId() {
    return carId;
  }

  public void setCarId(Long carId) {
    this.carId = carId;
  }
}
