package com.barbu.fleetmanagement.manager.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.barbu.fleetmanagement.manager.api.model.Driver;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "driver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="id")
  private Long id;
  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;
  @Column(name = "identification_number")
  private String identificationNumber;
  @Column(name = "birth_date")
  private LocalDate birthDate;
  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public void apply(Driver driver) {
    this.firstName = driver.firstName();
    this.lastName = driver.lastName();
    this.identificationNumber = driver.identificationNumber();
    this.birthDate = driver.birthDate();
  }
}
