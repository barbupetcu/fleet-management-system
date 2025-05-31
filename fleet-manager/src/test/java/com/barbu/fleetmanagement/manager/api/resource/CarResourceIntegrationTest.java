package com.barbu.fleetmanagement.manager.api.resource;

import com.barbu.fleetmanagement.manager.IntegrationTestResource;
import com.barbu.fleetmanagement.manager.api.model.Car;
import com.barbu.fleetmanagement.manager.domain.CarEntity;
import com.barbu.fleetmanagement.manager.domain.TripEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@Transactional
@QuarkusTest
@QuarkusTestResource(value = IntegrationTestResource.class)
public class CarResourceIntegrationTest {

    private static final String CARS_ENDPOINT = "/cars";

    @BeforeEach
    void beforeEach() {
        // Need to delete trips first due to foreign key constraints
        TripEntity.deleteAll();
        CarEntity.deleteAll();
    }

    @Test
    public void testGetCars() {
        Car car = createTestCar();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(car)
                .when()
                .put(CARS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Car createdCar = createResponse.as(Car.class);

        given()
                .when()
                .get(CARS_ENDPOINT + "?page_size=10&page=0&sort=id")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("content", not(empty()))
                .body("page", equalTo(0))
                .body("pageSize", equalTo(1));
    }

    @Test
    public void testCreateCar() {
        Car car = createTestCar();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(car)
                .when()
                .put(CARS_ENDPOINT)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();

        Car createdCar = response.as(Car.class);

        assertThat(createdCar.id(), notNullValue());
        assertThat(createdCar.model(), equalTo(car.model()));
        assertThat(createdCar.brand(), equalTo(car.brand()));
        assertThat(createdCar.colour(), equalTo(car.colour()));
        assertThat(createdCar.plateNumber(), equalTo(car.plateNumber()));
    }

    @Test
    public void testGetCar() {
        Car car = createTestCar();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(car)
                .when()
                .put(CARS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Car createdCar = createResponse.as(Car.class);

        given()
                .when()
                .get(CARS_ENDPOINT + "/" + createdCar.id())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(createdCar.id().intValue()))
                .body("model", equalTo(createdCar.model()))
                .body("brand", equalTo(createdCar.brand()))
                .body("colour", equalTo(createdCar.colour()))
                .body("plateNumber", equalTo(createdCar.plateNumber()));
    }

    @Test
    public void testDeleteCar() {
        Car car = createTestCar();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(car)
                .when()
                .put(CARS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Car createdCar = createResponse.as(Car.class);

        given()
                .when()
                .delete(CARS_ENDPOINT + "/" + createdCar.id())
                .then()
                .statusCode(204);

        given()
                .when()
                .get(CARS_ENDPOINT + "/" + createdCar.id())
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateCarWithInvalidData() {
        Car invalidCar = Car.builder()
                .model("") // Empty model
                .brand("Tesla")
                .colour("Red")
                .plateNumber("ABC123")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalidCar)
                .when()
                .put(CARS_ENDPOINT)
                .then()
                .statusCode(400); // Bad Request
    }

    @Test
    public void testCreateCarWithDuplicatePlateNumber() {
        Car car1 = createTestCar();

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(car1)
                .when()
                .put(CARS_ENDPOINT)
                .then()
                .statusCode(200)
                .extract().response();

        Car car2 = Car.builder()
                .model("Model Y")
                .brand("Tesla")
                .colour("Black")
                .plateNumber(car1.plateNumber())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(car2)
                .when()
                .put(CARS_ENDPOINT)
                .then()
                .statusCode(409); // Conflict - Car already exists
    }

    private Car createTestCar() {
        return Car.builder()
                .model("Model 3")
                .brand("Tesla")
                .colour("Red")
                .plateNumber("ABC" + System.currentTimeMillis()) // Ensure unique plate number
                .build();
    }
}
