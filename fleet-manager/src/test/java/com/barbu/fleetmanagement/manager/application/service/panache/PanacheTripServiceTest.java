package com.barbu.fleetmanagement.manager.application.service.panache;

import com.barbu.fleetmanagement.common.model.Location;
import com.barbu.fleetmanagement.common.model.Trip;
import com.barbu.fleetmanagement.manager.api.exception.CarNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.DriverNotFoundException;
import com.barbu.fleetmanagement.manager.api.exception.TripNotFoundException;
import com.barbu.fleetmanagement.manager.application.mapper.TripMapper;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanacheTripServiceTest {

    @Mock
    private TripMapper tripMapper;
    @Mock
    private TripEntity tripEntity;
    @Mock
    private Trip trip;
    @Mock
    private Emitter<Trip> tripEmitter;
    @Captor
    private ArgumentCaptor<Message<Trip>> tripMessageCaptor;
    
    @InjectMocks
    private PanacheTripService service;

    @Nested
    class CreateTrip {
        
        @Test
        void shouldPersistAndReturnMappedTrip() {
            when(tripMapper.to(trip)).thenReturn(tripEntity);
            
            Trip mappedTrip = mock(Trip.class);
            when(mappedTrip.driverId()).thenReturn(1L);
            when(tripMapper.from(tripEntity)).thenReturn(mappedTrip);
            
            Trip result = service.createTrip(trip);
            
            assertEquals(mappedTrip, result);
            verify(tripEntity).persistAndFlush();
            verify(tripMapper).from(tripEntity);

            verify(tripEmitter).send(tripMessageCaptor.capture());
            assertThat(tripMessageCaptor.getValue())
                    .returns(mappedTrip, Message::getPayload);
        }
        
        @Test
        void shouldThrowCarNotFoundExceptionOnCarForeignKeyViolation() {
            when(tripMapper.to(trip)).thenReturn(tripEntity);
            
            doThrow(new ConstraintViolationException(
                    "insert or update on table \"trip\" violates foreign key constraint \"fk_trip_car\"", 
                    null, "fk_trip_car"))
                    .when(tripEntity).persistAndFlush();
            
            assertThrows(CarNotFoundException.class, () -> service.createTrip(trip));
        }
        
        @Test
        void shouldThrowDriverNotFoundExceptionOnDriverForeignKeyViolation() {
            when(tripMapper.to(trip)).thenReturn(tripEntity);
            
            doThrow(new ConstraintViolationException(
                    "insert or update on table \"trip\" violates foreign key constraint \"fk_trip_driver\"", 
                    null, "fk_trip_driver"))
                    .when(tripEntity).persistAndFlush();
            
            assertThrows(DriverNotFoundException.class, () -> service.createTrip(trip));
        }
        
        @Test
        void shouldRethrowOtherConstraintViolations() {
            when(tripMapper.to(trip)).thenReturn(tripEntity);
            
            ConstraintViolationException exception = new ConstraintViolationException(
                    "some other constraint violation", null, "other_constraint");
            doThrow(exception).when(tripEntity).persistAndFlush();
            
            ConstraintViolationException thrown = assertThrows(ConstraintViolationException.class,
                    () -> service.createTrip(trip));
            assertEquals(exception, thrown);
        }
    }
    
    @Nested
    class FindTrip {
        
        @Test
        void shouldReturnMappedTripWhenFound() {
            Long tripId = 5L;
            Trip mappedTrip = createSampleTrip();
            when(tripMapper.from(tripEntity)).thenReturn(mappedTrip);
            
            try (MockedStatic<PanacheEntityBase> tripEntityMock = mockStatic(PanacheEntityBase.class)) {
                tripEntityMock.when(() -> TripEntity.findByIdOptional(tripId))
                        .thenReturn(Optional.of(tripEntity));
                
                Trip result = service.findTrip(tripId);
                
                assertEquals(mappedTrip, result);
                verify(tripMapper).from(tripEntity);
            }
        }
        
        @Test
        void shouldThrowTripNotFoundException() {
            Long tripId = 2L;
            
            try (MockedStatic<PanacheEntityBase> tripEntityMock = mockStatic(PanacheEntityBase.class)) {
                tripEntityMock.when(() -> TripEntity.findByIdOptional(tripId))
                        .thenReturn(Optional.empty());
                
                assertThrows(TripNotFoundException.class, () -> service.findTrip(tripId));
            }
        }
        
        private Trip createSampleTrip() {
            Location start = new Location(
                    new BigDecimal("40.7128"), 
                    new BigDecimal("-74.0060"));
            
            Location destination = new Location(
                    new BigDecimal("34.0522"), 
                    new BigDecimal("-118.2437"));
            
            return Trip.builder()
                    .start(start)
                    .destination(destination)
                    .driverId(1L)
                    .carId(2L)
                    .build();
        }
    }
}