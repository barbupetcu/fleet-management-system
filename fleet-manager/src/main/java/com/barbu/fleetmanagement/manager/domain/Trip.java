package com.barbu.fleetmanagement.manager.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trip")
public class Trip extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @Column(name="id")
  private Long id;
  @Column(name="latitude_start")
  private double latitudeStart;
  @Column(name="longitude_start")
  private double longitudeStart;
  @Column(name="latitude_destination")
  private double latitudeDestination;
  @Column(name="longitude_destination")
  private double longitudeDestination;
  @Column(name="driver_id")
  private Long driverId;
  @Column(name="car_id")
  private Long carId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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
