package com.barbu.fleetmanagement.manager.application.mapper;

import com.barbu.fleetmanagement.manager.api.model.Car;
import com.barbu.fleetmanagement.manager.domain.CarEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CarMapper {

    public CarEntity to(Car car) {
        return CarEntity.builder()
                .id(car.id())
                .model(car.model())
                .brand(car.brand())
                .colour(car.colour())
                .plateNumber(car.plateNumber())
                .build();
    }

    public Car from(CarEntity entity) {
        return Car.builder()
                .id(entity.getId())
                .model(entity.getModel())
                .brand(entity.getBrand())
                .colour(entity.getColour())
                .plateNumber(entity.getPlateNumber())
                .build();
    }
}