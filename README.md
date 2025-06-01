# Fleet Management System

A microservices-based system for managing a fleet of vehicles, tracking trips, and calculating penalty points for speeding.

## System Overview

The Fleet Management System consists of three microservices that work together to manage a fleet of vehicles, simulate trips, and calculate penalty points for drivers who exceed speed limits.

## Microservices

### Fleet Manager

The Fleet Manager microservice is responsible for managing the core entities of the system:
- Cars: Information about vehicles in the fleet
- Drivers: Information about drivers
- Trips: Information about trips taken by drivers in fleet vehicles

This service provides REST APIs for CRUD operations on these entities and stores the data in a PostgreSQL database.

### TODO's:
- Add an endpoint which can delete/cancel the trip and publish an event informing trip-simulator to cancel/stop the trip
- Add validation to not allow creating a new trip for a car or a driver who has a trip in progress
- Add Car minimum and maximum speed limits which could influence the randomization of speeds from trip-simulator

### Trip Simulator

The Trip Simulator microservice simulates car movements during trips:
- Receives trip information from the Fleet Manager
- Simulates car movement along a route from start to destination
- Generates car position updates with random speeds (between 40-120 km/h)
- Publishes car position events to Kafka

This service uses geographic calculations to simulate realistic car movement and speed variations.

### TODO's:
- publish events when the trip is finished, which can be consumed by fleet-manager to allow creation of new trips for the same driver/car
- store car positions in a database
- use persistent scheduler to update car positions like Quartz or Schedlock

### Penalty Points Calculator

The Penalty Points Calculator microservice calculates penalty points for speeding:
- Consumes car position events from Kafka
- Calculates car speeds based on consecutive position updates
- Assigns penalty points based on speed intervals:
  - 60-79 km/h: 2 penalty points per km
  - 80+ km/h: 5 penalty points per km
- Aggregates penalty points per driver
- Publishes driver penalty point events to Kafka

This service uses Kafka Streams for real-time stream processing of car position data.


### TODO's:
- Store previous position in a distributed caching system (redis) with TTL
- Add tests

## Data Flow

1. The Fleet Manager creates and stores information about cars, drivers, and trips
2. When a trip is created, the Trip Simulator starts simulating the car's movement
3. The Trip Simulator publishes car position events to Kafka
4. The Penalty Points Calculator consumes car position events and calculates speeds
5. If a car is speeding, the Penalty Points Calculator assigns penalty points
6. The Penalty Points Calculator publishes driver penalty point events to Kafka and publishes also a snapshot of total points for the driver
7. The Fleet Manager can consume these events to update driver records (TODO)

## Technologies Used

- **Java 24**: Programming language
- **Quarkus**: Cloud-native Java framework
- **Kafka**: Event streaming platform for inter-service communication
- **Kafka Streams**: Stream processing library for real-time data processing
- **PostgreSQL**: Relational database for persistent storage
- **Docker**: Containerization for deployment
- **Maven**: Build and dependency management

## Setup and Deployment

The system can be deployed using Docker Compose:

```bash
mvn install
docker-compose up -d
```

This will start all the required services:
- PostgreSQL database
- Zookeeper and Kafka
- Fleet Manager microservice
- Trip Simulator microservice
- Penalty Points Calculator microservice

## Usage example

1. Create a driver
```bash
curl --location --request PUT 'http://localhost:8080/drivers' \
--header 'Content-Type: application/json' \
--data '{
    "firstName": "Test",
    "lastName": "User",
    "identificationNumber": "123",
    "birthDate": "1989-10-05"
}' 
```
2. Create a car
```bash
 curl --location --request PUT 'http://localhost:8080/cars' \
--header 'Content-Type: application/json' \
--data '{

    "model": "Model 3",
    "brand": "Tesla",
    "colour": "black",
    "plateNumber": "B89EAS"
}'
```
3. Create a trip
```bash
curl --location --request PUT 'http://localhost:8080/trips' \
--header 'Content-Type: application/json' \
--data '{
    "start": {
        "latitude": 44.720965,
        "longitude": 26.606699
    },
    "destination": {
        "latitude": 44.777532,
        "longitude": 26.505762
    },
    "driverId": "1",
    "carId": "1"
}'
```