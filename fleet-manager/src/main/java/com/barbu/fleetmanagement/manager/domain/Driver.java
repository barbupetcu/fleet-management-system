package com.barbu.fleetmanagement.manager.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class Driver extends PanacheEntity {

  private String firstName;
  private String lastName;
  private String identificationNumber;
  private LocalDateTime birthDate;

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getIdentificationNumber() {
    return identificationNumber;
  }

  public LocalDateTime getBirthDate() {
    return birthDate;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setIdentificationNumber(String identificationNumber) {
    this.identificationNumber = identificationNumber;
  }

  public void setBirthDate(LocalDateTime birthDate) {
    this.birthDate = birthDate;
  }
}
