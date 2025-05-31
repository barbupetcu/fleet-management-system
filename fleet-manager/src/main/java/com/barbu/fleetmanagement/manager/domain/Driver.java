package com.barbu.fleetmanagement.manager.domain;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "driver")
public class Driver extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @Column(name="id")
  private Long id;
  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;
  @Column(name = "identification_number")
  private String identificationNumber;
  @Column(name = "birth_date")
  private LocalDateTime birthDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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
