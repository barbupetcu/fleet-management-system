package com.barbu.fleetmanagement.manager.application.mapper;

import com.barbu.fleetmanagement.manager.api.model.Driver;
import com.barbu.fleetmanagement.manager.domain.DriverEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DriverMapper {

    public DriverEntity to(Driver driver) {
        return DriverEntity.builder()
                .id(driver.id())
                .birthDate(driver.birthDate())
                .firstName(driver.firstName())
                .lastName(driver.lastName())
                .identificationNumber(driver.identificationNumber())
                .build();
    }

    public Driver from(DriverEntity entity) {
        return Driver.builder()
                .id(entity.getId())
                .birthDate(entity.getBirthDate())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .identificationNumber(entity.getIdentificationNumber())
                .build();
    }
}
