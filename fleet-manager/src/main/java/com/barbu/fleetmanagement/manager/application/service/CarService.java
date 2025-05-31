package com.barbu.fleetmanagement.manager.application.service;

import com.barbu.fleetmanagement.manager.api.model.Car;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;

import java.util.Set;

public interface CarService {

    Car createCar(Car car);

    Car findCar(Long id);

    void deleteCar(Long id);

    PaginatedResponse<Car> findCar(int pageSize, int page, Set<String> sort);
}