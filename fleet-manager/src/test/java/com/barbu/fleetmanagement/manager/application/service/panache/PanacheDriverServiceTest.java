package com.barbu.fleetmanagement.manager.application.service.panache;

import com.barbu.fleetmanagement.manager.api.exception.DriverAlreadyExistsException;
import com.barbu.fleetmanagement.manager.api.exception.DriverNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.InvalidDriverException;
import com.barbu.fleetmanagement.manager.api.model.Driver;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;
import com.barbu.fleetmanagement.manager.application.mapper.DriverMapper;
import com.barbu.fleetmanagement.manager.domain.DriverEntity;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanacheDriverServiceTest {

    @Mock
    private DriverMapper driverMapper;
    @Mock
    private DriverEntity driverEntity;
    @Mock
    private Driver driver;
    @Mock
    private PanacheQuery<DriverEntity> panacheQuery;
    @InjectMocks
    private PanacheDriverService service;

    @Nested
    class CreateDriver {
        @Test
        void shouldThrowInvalidDriverExceptionWhenUnder18() {
            when(driver.birthDate()).thenReturn(LocalDate.now().minusYears(17));

            InvalidDriverException ex = assertThrows(InvalidDriverException.class, () ->
                    service.createDriver(driver));
            assertTrue(ex.getMessage().contains("18 years"));
        }

        @Test
        void shouldPersistAndReturnMappedDriver() {
            when(driver.birthDate()).thenReturn(LocalDate.now().minusYears(20));
            when(driver.id()).thenReturn(null);

            when(driverMapper.to(driver)).thenReturn(driverEntity);

            Driver mappedDriver = mock(Driver.class);
            when(driverMapper.from(driverEntity)).thenReturn(mappedDriver);

            Driver result = service.createDriver(driver);
            assertEquals(mappedDriver, result);
            verify(driverEntity).persistAndFlush();
            verify(driverMapper).from(driverEntity);
        }

        @Test
        void shouldThrowDriverAlreadyExistsExceptionOnDuplicateKey() {
            when(driver.birthDate()).thenReturn(LocalDate.now().minusYears(30));
            when(driver.id()).thenReturn(null);

            when(driverMapper.to(driver)).thenReturn(driverEntity);

            doThrow(new ConstraintViolationException(
                    "duplicate key value violates unique constraint \"driver_identification_number_key\"", null, "driver_identification_number_key"))
                    .when(driverEntity).persistAndFlush();

            assertThrows(DriverAlreadyExistsException.class, () -> service.createDriver(driver));
        }
    }

    @Nested
    class FindDriver {
        @Test
        void shouldReturnMappedDriverWhenFound() {
            Long driverId = 5L;
            Driver mapped = mock(Driver.class);
            when(driverMapper.from(driverEntity)).thenReturn(mapped);

            // static mocking
            try (MockedStatic<PanacheEntityBase> driverEntityMock = mockStatic(PanacheEntityBase.class)) {
                driverEntityMock.when(() -> DriverEntity.findByIdOptional(driverId))
                        .thenReturn(Optional.of(driverEntity));
                Driver result = service.findDriver(driverId);
                assertEquals(mapped, result);
            }
        }

        @Test
        void shouldThrowDriverNotFoundException() {
            Long driverId = 2L;
            try (MockedStatic<PanacheEntityBase> driverEntityMock = mockStatic(PanacheEntityBase.class)) {
                driverEntityMock.when(() -> DriverEntity.findByIdOptional(driverId))
                        .thenReturn(Optional.empty());
                assertThrows(DriverNotFoundException.class, () -> service.findDriver(driverId));
            }
        }

        @Test
        void shouldReturnPaginatedResponse() {
            int pageSize = 2, pageIndex = 1;
            long total = 3;
            Set<String> sortSet = Set.of("name");
            DriverEntity entity1 = new DriverEntity();
            DriverEntity entity2 = new DriverEntity();

            Driver driver1 = Driver.builder().build();
            Driver driver2 = Driver.builder().build();

            when(panacheQuery.stream()).thenReturn(Stream.of(entity1, entity2));

            try (MockedStatic<PanacheEntityBase> driverEntityMock = mockStatic(PanacheEntityBase.class)) {
                driverEntityMock.when(() -> DriverEntity.findAll(any(Sort.class))).thenReturn(panacheQuery);
                driverEntityMock.when(DriverEntity::count).thenReturn(total);

                when(driverMapper.from(entity1)).thenReturn(driver1);
                when(driverMapper.from(entity2)).thenReturn(driver2);

                PaginatedResponse<Driver> response = service.findDriver(pageSize, pageIndex, sortSet);

                verify(panacheQuery).page(any(Page.class));
                assertThat(response)
                        .returns(pageSize, PaginatedResponse::pageSize)
                        .returns(pageIndex, PaginatedResponse::page)
                        .returns(total, PaginatedResponse::total)
                        .returns(List.of(driver1, driver2), PaginatedResponse::content);

            }
        }
    }

    @Nested
    class DeleteDriver {
        @Test
        void shouldCallDeleteMethods() {
            Long driverId = 3L;
            try (
                    MockedStatic<PanacheEntityBase> panacheEntityMock = mockStatic(PanacheEntityBase.class);
            ) {
                service.deleteDriver(driverId);
                panacheEntityMock.verify(() -> TripEntity.delete("driverId", driverId));
                panacheEntityMock.verify(() -> DriverEntity.deleteById(driverId));
            }
        }
    }
}