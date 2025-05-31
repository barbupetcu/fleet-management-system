package com.barbu.fleetmanagement.manager.application.service.panache;

import com.barbu.fleetmanagement.manager.api.exception.CarAlreadyExistsException;
import com.barbu.fleetmanagement.manager.api.exception.CarNotFoundException;
import com.barbu.fleetmanagement.manager.api.model.Car;
import com.barbu.fleetmanagement.manager.api.model.PaginatedResponse;
import com.barbu.fleetmanagement.manager.application.mapper.CarMapper;
import com.barbu.fleetmanagement.manager.domain.CarEntity;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanacheCarServiceTest {

    @Mock
    private CarMapper carMapper;
    @Mock
    private CarEntity carEntity;
    @Mock
    private Car car;
    @Mock
    private PanacheQuery<CarEntity> panacheQuery;
    @InjectMocks
    private PanacheCarService service;

    @Nested
    class CreateCar {
        @Test
        void shouldCreateNewCarWhenIdIsNull() {
            when(car.id()).thenReturn(null);
            when(carMapper.to(car)).thenReturn(carEntity);
            when(carMapper.from(carEntity)).thenReturn(car);

            Car result = service.createCar(car);

            verify(carEntity).persistAndFlush();
            verify(carMapper).from(carEntity);
            assertEquals(car, result);
        }

        @Test
        void shouldUpdateExistingCarWhenIdIsNotNull() {
            Long carId = 1L;
            when(car.id()).thenReturn(carId);
            
            try (MockedStatic<PanacheEntityBase> carEntityMock = mockStatic(PanacheEntityBase.class)) {
                carEntityMock.when(() -> CarEntity.findById(carId)).thenReturn(carEntity);
                when(carMapper.from(carEntity)).thenReturn(car);

                Car result = service.createCar(car);

                verify(carEntity).apply(car);
                verify(carEntity).persistAndFlush();
                verify(carMapper).from(carEntity);
                assertEquals(car, result);
            }
        }

        @Test
        void shouldThrowCarNotFoundExceptionWhenUpdatingNonExistentCar() {
            Long carId = 1L;
            when(car.id()).thenReturn(carId);
            
            try (MockedStatic<PanacheEntityBase> carEntityMock = mockStatic(PanacheEntityBase.class)) {
                carEntityMock.when(() -> CarEntity.findById(carId)).thenReturn(null);

                assertThrows(CarNotFoundException.class, () -> service.createCar(car));
            }
        }

        @Test
        void shouldThrowCarAlreadyExistsExceptionOnDuplicatePlateNumber() {
            when(car.id()).thenReturn(null);
            when(carMapper.to(car)).thenReturn(carEntity);
            
            ConstraintViolationException exception = mock(ConstraintViolationException.class);
            when(exception.getMessage()).thenReturn("duplicate key value violates unique constraint \"car_plate_number_key\"");
            doThrow(exception).when(carEntity).persistAndFlush();

            assertThrows(CarAlreadyExistsException.class, () -> service.createCar(car));
        }
    }

    @Nested
    class FindCar {
        @Test
        void shouldReturnCarWhenFoundById() {
            Long carId = 1L;
            
            try (MockedStatic<PanacheEntityBase> carEntityMock = mockStatic(PanacheEntityBase.class)) {
                carEntityMock.when(() -> CarEntity.findByIdOptional(carId)).thenReturn(Optional.of(carEntity));
                when(carMapper.from(carEntity)).thenReturn(car);

                Car result = service.findCar(carId);

                assertEquals(car, result);
                verify(carMapper).from(carEntity);
            }
        }

        @Test
        void shouldThrowCarNotFoundExceptionWhenNotFoundById() {
            Long carId = 1L;
            
            try (MockedStatic<PanacheEntityBase> carEntityMock = mockStatic(PanacheEntityBase.class)) {
                carEntityMock.when(() -> CarEntity.findByIdOptional(carId)).thenReturn(Optional.empty());

                assertThrows(CarNotFoundException.class, () -> service.findCar(carId));
            }
        }

        @Test
        void shouldReturnPaginatedResponseWhenFindingCarsWithPagination() {
            int pageSize = 10;
            int page = 0;
            Set<String> sort = Set.of("brand");
            long total = 20L;
            
            CarEntity carEntity1 = mock(CarEntity.class);
            CarEntity carEntity2 = mock(CarEntity.class);
            Car car1 = mock(Car.class);
            Car car2 = mock(Car.class);
            List<Car> cars = List.of(car1, car2);
            
            try (MockedStatic<PanacheEntityBase> carEntityMock = mockStatic(PanacheEntityBase.class)) {
                carEntityMock.when(() -> CarEntity.findAll(any(Sort.class))).thenReturn(panacheQuery);
                carEntityMock.when(CarEntity::count).thenReturn(total);
                
                when(panacheQuery.stream()).thenReturn(Stream.of(carEntity1, carEntity2));
                when(carMapper.from(carEntity1)).thenReturn(car1);
                when(carMapper.from(carEntity2)).thenReturn(car2);

                PaginatedResponse<Car> result = service.findCar(pageSize, page, sort);

                verify(panacheQuery).page(any(Page.class));
                assertThat(result)
                    .returns(cars, PaginatedResponse::content)
                    .returns(page, PaginatedResponse::page)
                    .returns(cars.size(), PaginatedResponse::pageSize)
                    .returns(total, PaginatedResponse::total);
            }
        }
    }

    @Nested
    class DeleteCar {
        @Test
        void shouldDeleteCarById() {
            Long carId = 1L;
            
            try (MockedStatic<PanacheEntityBase> carEntityMock = mockStatic(PanacheEntityBase.class)) {
                service.deleteCar(carId);
                carEntityMock.verify(() -> CarEntity.deleteById(carId));
            }
        }
    }
}