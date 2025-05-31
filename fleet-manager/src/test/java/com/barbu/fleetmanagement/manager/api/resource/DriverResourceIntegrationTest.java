package com.barbu.fleetmanagement.manager.api.resource;

import com.barbu.fleetmanagement.manager.IntegrationTestResource;
import com.barbu.fleetmanagement.manager.api.model.Driver;
import com.barbu.fleetmanagement.manager.domain.DriverEntity;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@Transactional
@QuarkusTest
@QuarkusTestResource(value = IntegrationTestResource.class)
public class DriverResourceIntegrationTest {

    private static final String DRIVERS_ENDPOINT = "/drivers";

    @BeforeEach
    void beforeEach() {
        TripEntity.deleteAll();
        DriverEntity.deleteAll();
    }

    @Test
    public void testGetDrivers() {
        // Create a test driver
        Driver driver = createTestDriver();

        // Create the driver using the API
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(driver)
                .when()
                .put(DRIVERS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Driver createdDriver = createResponse.as(Driver.class);

        // Test the GET /drivers endpoint
        given()
                .when()
                .get(DRIVERS_ENDPOINT + "?page_size=10&page=0&sort=id")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content", not(empty()))
                .body("page", equalTo(0))
                .body("pageSize", equalTo(1));
    }

    @Test
    public void testCreateDriver() {
        Driver driver = createTestDriver();

        // Test the PUT /drivers endpoint
        Response response = given()
                .contentType(ContentType.JSON)
                .body(driver)
                .when()
                .put(DRIVERS_ENDPOINT)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        Driver createdDriver = response.as(Driver.class);

        assertThat(createdDriver.id(), notNullValue());
        assertThat(createdDriver.firstName(), equalTo(driver.firstName()));
        assertThat(createdDriver.lastName(), equalTo(driver.lastName()));
        assertThat(createdDriver.identificationNumber(), equalTo(driver.identificationNumber()));
        assertThat(createdDriver.birthDate(), equalTo(driver.birthDate()));
    }

    @Test
    public void testGetDriver() {
        // Create a test driver
        Driver driver = createTestDriver();

        // Create the driver using the API
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(driver)
                .when()
                .put(DRIVERS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Driver createdDriver = createResponse.as(Driver.class);

        // Test the GET /drivers/{id} endpoint
        given()
                .when()
                .get(DRIVERS_ENDPOINT + "/" + createdDriver.id())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(createdDriver.id().intValue()))
                .body("firstName", equalTo(createdDriver.firstName()))
                .body("lastName", equalTo(createdDriver.lastName()))
                .body("identificationNumber", equalTo(createdDriver.identificationNumber()))
                .body("birthDate", equalTo(createdDriver.birthDate().toString()));
    }

    @Test
    public void testDeleteDriver() {
        // Create a test driver
        Driver driver = createTestDriver();

        // Create the driver using the API
        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(driver)
                .when()
                .put(DRIVERS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Driver createdDriver = createResponse.as(Driver.class);

        // Test the DELETE /drivers/{id} endpoint
        given()
                .when()
                .delete(DRIVERS_ENDPOINT + "/" + createdDriver.id())
                .then()
                .statusCode(204);

        // Verify the driver was deleted
        given()
                .when()
                .get(DRIVERS_ENDPOINT + "/" + createdDriver.id())
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateDriverWithInvalidData() {
        // Missing required fields
        Driver invalidDriver = Driver.builder()
                .firstName("") // Empty first name
                .lastName("Doe")
                .identificationNumber("ID123456")
                .birthDate(LocalDate.now().minusYears(20))
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidDriver)
                .when()
                .put(DRIVERS_ENDPOINT)
                .then()
                .statusCode(400); // Bad Request
    }

    @Test
    public void testCreateDriverUnder18() {
        // Driver under 18 years old
        Driver underageDriver = Driver.builder()
                .firstName("Young")
                .lastName("Driver")
                .identificationNumber("ID789012")
                .birthDate(LocalDate.now().minusYears(17))
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(underageDriver)
                .when()
                .put(DRIVERS_ENDPOINT)
                .then()
                .statusCode(400);
    }

    private Driver createTestDriver() {
        return Driver.builder()
                .firstName("John")
                .lastName("Doe")
                .identificationNumber("ID" + System.currentTimeMillis()) // Ensure unique ID
                .birthDate(LocalDate.now().minusYears(30))
                .build();
    }
}
