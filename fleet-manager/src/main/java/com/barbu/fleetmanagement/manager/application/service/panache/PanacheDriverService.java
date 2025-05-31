package com.barbu.fleetmanagement.manager.application.service.panache;

import com.barbu.fleetmanagement.manager.api.exception.DriverAlreadyExistsException;
import com.barbu.fleetmanagement.manager.api.exception.DriverNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.InvalidDriverException;
import com.barbu.fleetmanagement.manager.api.model.Driver;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;
import com.barbu.fleetmanagement.manager.application.mapper.DriverMapper;
import com.barbu.fleetmanagement.manager.application.service.DriverService;
import com.barbu.fleetmanagement.manager.domain.DriverEntity;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class PanacheDriverService implements DriverService {

    private final DriverMapper driverMapper;

    @Override
    public Driver createDriver(Driver driver) {
        if(LocalDate.now().minusYears(18).isBefore(driver.birthDate())) {
            throw new InvalidDriverException("The driver must have at least 18 years");
        }
        DriverEntity driverEntity;
        if (driver.id() == null) {
            driverEntity = driverMapper.to(driver);
        } else {
            driverEntity = updateDriver(driver);
        }
        save(driverEntity);

        return driverMapper.from(driverEntity);
    }

    private DriverEntity updateDriver(Driver driver) {
        DriverEntity driverEntity;
        driverEntity = DriverEntity.findById(driver.id());
        if (driverEntity == null) {
            throw new DriverNotFoundException();
        }
        driverEntity.apply(driver);
        return driverEntity;
    }

    private static void save(DriverEntity driverEntity) {
        try {
            driverEntity.persistAndFlush();
        } catch (ConstraintViolationException e) {
            if (e.getMessage().contains("duplicate key value violates unique constraint " +
                    "\"driver_identification_number_key\"")) {
                throw new DriverAlreadyExistsException();
            } else {
                throw e;
            }
        }
    }

    @Override
    public Driver findDriver(Long id) {
        return DriverEntity.findByIdOptional(id)
                .map(DriverEntity.class::cast)
                .map(driverMapper::from)
                .orElseThrow(DriverNotFoundException::new);
    }

    @Override
    public void deleteDriver(Long id) {
        TripEntity.delete("driverId", id);
        DriverEntity.deleteById(id);
    }

    @Override
    public PaginatedResponse<Driver> findDriver(int pageSize, int page, Set<String> sort) {
        PanacheQuery<DriverEntity> driversQuery = DriverEntity.findAll(Sort.by(sort.toArray(new String[0])));
        driversQuery.page(Page.of(page, pageSize));

        List<Driver> drivers = driversQuery.stream()
                .map(driverMapper::from)
                .toList();
        return new PaginatedResponse<>(drivers, page, drivers.size(), DriverEntity.count());
    }
}
