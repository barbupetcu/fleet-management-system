package com.barbu.fleetmanagement.manager.api.resource;

import com.barbu.fleetmanagement.common.model.Location;
import com.barbu.fleetmanagement.common.model.Trip;
import com.barbu.fleetmanagement.manager.IntegrationTestResource;
import com.barbu.fleetmanagement.manager.domain.CarEntity;
import com.barbu.fleetmanagement.manager.domain.DriverEntity;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Transactional
@QuarkusTest
@QuarkusTestResource(value = IntegrationTestResource.class)
public class TripResourceIntegrationTest {

    private static final String TRIPS_ENDPOINT = "/trips";

    private Long driverId;
    private Long carId;

    @BeforeEach
    void beforeEach() {
        TripEntity.deleteAll();
        CarEntity.deleteAll();
        DriverEntity.deleteAll();

        driverId = createTestDriver();
        carId = createTestCar();
    }

    private Long createTestDriver() {
        DriverEntity driverEntity = DriverEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .identificationNumber("ID" + System.currentTimeMillis()) // Ensure unique ID
                .birthDate(LocalDate.now().minusYears(30))
                .build();
        driverEntity.persistAndFlush();
        return driverEntity.getId();
    }

    private Long createTestCar() {
        CarEntity carEntity = CarEntity.builder()
                .brand("Tesla")
                .model("Model 3")
                .colour("blue")
                .plateNumber("B" + System.currentTimeMillis()) // Ensure unique plate number
                .build();
        carEntity.persistAndFlush();
        return carEntity.getId();
    }

    @Test
    public void testCreateTrip() {
        Trip trip = createTestTrip();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(trip)
                .when()
                .put(TRIPS_ENDPOINT)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        Trip createdTrip = response.as(Trip.class);

        assertThat(createdTrip.start(), notNullValue());
        assertThat(createdTrip.destination(), notNullValue());
        assertThat(createdTrip.start().latitude(), equalTo(trip.start().latitude()));
        assertThat(createdTrip.start().longitude(), equalTo(trip.start().longitude()));
        assertThat(createdTrip.destination().latitude(), equalTo(trip.destination().latitude()));
        assertThat(createdTrip.destination().longitude(), equalTo(trip.destination().longitude()));
        assertThat(createdTrip.driverId(), equalTo(trip.driverId()));
        assertThat(createdTrip.carId(), equalTo(trip.carId()));
    }

    @Test
    public void testGetTrip() {
        Trip trip = createTestTrip();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(trip)
                .when()
                .put(TRIPS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Trip createdTrip = createResponse.as(Trip.class);

        TripEntity tripEntity = TripEntity.find("driverId = ?1 and carId = ?2", driverId, carId).firstResult();
        assertThat(tripEntity, notNullValue());

        given()
                .when()
                .get(TRIPS_ENDPOINT + "/" + tripEntity.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("start.latitude", equalTo(createdTrip.start().latitude().floatValue()))
                .body("start.longitude", equalTo(createdTrip.start().longitude().floatValue()))
                .body("destination.latitude", equalTo(createdTrip.destination().latitude().floatValue()))
                .body("destination.longitude", equalTo(createdTrip.destination().longitude().floatValue()))
                .body("driverId", equalTo(createdTrip.driverId().intValue()))
                .body("carId", equalTo(createdTrip.carId().intValue()));
    }

    @Test
    public void testCreateTripWithInvalidData() {
        Trip invalidTrip = Trip.builder()
                .start(null)
                .destination(new Location(BigDecimal.valueOf(40.7128), BigDecimal.valueOf(-74.0060)))
                .driverId(driverId)
                .carId(carId)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidTrip)
                .when()
                .put(TRIPS_ENDPOINT)
                .then()
                .statusCode(400); // Bad Request
    }

    private Trip createTestTrip() {
        return Trip.builder()
                .start(new Location(BigDecimal.valueOf(37.7749), BigDecimal.valueOf(-122.4194))) // San Francisco
                .destination(new Location(BigDecimal.valueOf(34.0522), BigDecimal.valueOf(-118.2437))) // Los Angeles
                .driverId(driverId)
                .carId(carId)
                .build();
    }
}
