package com.barbu.fleetmanagement.manager.application.mapper;

import com.barbu.fleetmanagement.manager.api.model.Car;
import com.barbu.fleetmanagement.manager.domain.CarEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CarMapperTest {

    private final CarMapper carMapper = new CarMapper();

    @Test
    void toCarEntity() {
        Long id = 1L;
        String model = "Model 3";
        String brand = "Tesla";
        String colour = "Red";
        String plateNumber = "ABC123";
        
        Car car = Car.builder()
                .id(id)
                .model(model)
                .brand(brand)
                .colour(colour)
                .plateNumber(plateNumber)
                .build();
        
        CarEntity carEntity = carMapper.to(car);

        assertThat(carEntity)
                .returns(id, CarEntity::getId)
                .returns(model, CarEntity::getModel)
                .returns(brand, CarEntity::getBrand)
                .returns(colour, CarEntity::getColour)
                .returns(plateNumber, CarEntity::getPlateNumber);
    }

    
    @Test
    void fromCarEntity() {
        Long id = 1L;
        String model = "Model 3";
        String brand = "Tesla";
        String colour = "Red";
        String plateNumber = "ABC123";
        
        CarEntity carEntity = CarEntity.builder()
                .id(id)
                .model(model)
                .brand(brand)
                .colour(colour)
                .plateNumber(plateNumber)
                .build();
        
        Car car = carMapper.from(carEntity);

        assertThat(car)
                .returns(id, Car::id)
                .returns(model, Car::model)
                .returns(brand, Car::brand)
                .returns(colour, Car::colour)
                .returns(plateNumber, Car::plateNumber);
    }
}