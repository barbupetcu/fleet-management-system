package com.barbu.fleetmanagement.manager.application.service;

import com.barbu.fleetmanagement.manager.api.model.Driver;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;

import java.util.Set;

public interface DriverService {

    Driver createDriver(Driver driver);

    Driver findDriver(Long id);

    void deleteDriver(Long id);

    PaginatedResponse<Driver> findDriver(int pageSize, int page, Set<String> sort);
}
