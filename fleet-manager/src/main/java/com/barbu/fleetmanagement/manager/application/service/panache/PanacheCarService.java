package com.barbu.fleetmanagement.manager.application.service.panache;

import com.barbu.fleetmanagement.manager.api.exception.CarAlreadyExistsException;
import com.barbu.fleetmanagement.manager.api.exception.CarNotFoundException;
import com.barbu.fleetmanagement.manager.api.model.Car;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;
import com.barbu.fleetmanagement.manager.application.mapper.CarMapper;
import com.barbu.fleetmanagement.manager.application.service.CarService;
import com.barbu.fleetmanagement.manager.domain.CarEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Set;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class PanacheCarService implements CarService {

    private final CarMapper carMapper;

    @Override
    public Car createCar(Car car) {
        CarEntity carEntity;
        if (car.id() == null) {
            carEntity = carMapper.to(car);
        } else {
            carEntity = updateCar(car);
        }
        save(carEntity);

        return carMapper.from(carEntity);
    }

    private CarEntity updateCar(Car car) {
        CarEntity carEntity;
        carEntity = CarEntity.findById(car.id());
        if (carEntity == null) {
            throw new CarNotFoundException();
        }
        carEntity.apply(car);
        return carEntity;
    }

    private static void save(CarEntity carEntity) {
        try {
            carEntity.persistAndFlush();
        } catch (ConstraintViolationException e) {
            if (e.getMessage().contains("duplicate key value violates unique constraint " +
                    "\"car_plate_number_key\"")) {
                throw new CarAlreadyExistsException();
            } else {
                throw e;
            }
        }
    }

    @Override
    public Car findCar(Long id) {
        return CarEntity.findByIdOptional(id)
                .map(CarEntity.class::cast)
                .map(carMapper::from)
                .orElseThrow(CarNotFoundException::new);
    }

    @Override
    public void deleteCar(Long id) {
        CarEntity.deleteById(id);
    }

    @Override
    public PaginatedResponse<Car> findCar(int pageSize, int page, Set<String> sort) {
        PanacheQuery<CarEntity> carsQuery = CarEntity.findAll(Sort.by(sort.toArray(new String[0])));
        carsQuery.page(Page.of(page, pageSize));

        List<Car> cars = carsQuery.stream()
                .map(carMapper::from)
                .toList();
        return new PaginatedResponse<>(cars, page, cars.size(), CarEntity.count());
    }
}