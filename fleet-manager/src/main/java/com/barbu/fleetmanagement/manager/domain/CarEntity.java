package com.barbu.fleetmanagement.manager.domain;

import com.barbu.fleetmanagement.manager.api.model.Car;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "car")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarEntity extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="id")
  private Long id;

  @Column(name = "model")
  private String model;
  @Column(name = "brand")
  private String brand;
  @Column(name = "colour")
  private String colour;
  @Column(name = "plate_number")
  private String plateNumber;
  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public void apply(Car car) {
    this.model = car.model();
    this.brand = car.brand();
    this.colour = car.colour();
    this.plateNumber = car.plateNumber();
  }
}
