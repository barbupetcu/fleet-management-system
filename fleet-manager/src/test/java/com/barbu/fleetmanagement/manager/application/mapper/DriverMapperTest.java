package com.barbu.fleetmanagement.manager.application.mapper;

import com.barbu.fleetmanagement.manager.api.model.Driver;
import com.barbu.fleetmanagement.manager.domain.DriverEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverMapperTest {

    private final DriverMapper driverMapper = new DriverMapper();

    @Test
    void toDriverEntity() {
        Long id = 1L;
        String firstName = "John";
        String lastName = "Doe";
        String identificationNumber = "ABC123";
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        
        Driver driver = Driver.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .identificationNumber(identificationNumber)
                .birthDate(birthDate)
                .build();
        
        DriverEntity driverEntity = driverMapper.to(driver);

        assertThat(driverEntity)
                .returns(id, DriverEntity::getId)
                .returns(firstName, DriverEntity::getFirstName)
                .returns(lastName, DriverEntity::getLastName)
                .returns(identificationNumber, DriverEntity::getIdentificationNumber)
                .returns(birthDate, DriverEntity::getBirthDate);
    }

    
    @Test
    void fromDriverEntity() {
        Long id = 1L;
        String firstName = "John";
        String lastName = "Doe";
        String identificationNumber = "ABC123";
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        
        DriverEntity driverEntity = DriverEntity.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .identificationNumber(identificationNumber)
                .birthDate(birthDate)
                .build();
        
        Driver driver = driverMapper.from(driverEntity);

        assertThat(driver)
                .returns(id, Driver::id)
                .returns(firstName, Driver::firstName)
                .returns(lastName, Driver::lastName)
                .returns(identificationNumber, Driver::identificationNumber)
                .returns(birthDate, Driver::birthDate);
    }

}