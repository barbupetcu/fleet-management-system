# Fleet Management System

A fleet management system built with Quarkus, jOOQ, and Liquibase.

## Project Structure

- **fleet-api**: Quarkus REST API module with jOOQ and Liquibase
  - REST endpoints for vehicle management
  - PostgreSQL database integration
  - Liquibase for database migrations
  - jOOQ for type-safe SQL queries

## Technologies

- **Quarkus**: Supersonic Subatomic Java Framework - <https://quarkus.io/>
- **jOOQ**: Java Object Oriented Querying - <https://www.jooq.org/>
- **Liquibase**: Database schema migration tool - <https://www.liquibase.org/>
- **PostgreSQL**: Open-source relational database - <https://www.postgresql.org/>

## Getting Started

### Prerequisites

- Java 21
- Maven
- Docker and Docker Compose

### Running with Docker Compose

The easiest way to run the entire application stack is using Docker Compose:

```shell script
docker-compose up -d
```

This will start:
- PostgreSQL database
- Fleet Management API (Quarkus application)

The API will be available at: http://localhost:8080/api/vehicles

### Running in Dev Mode

You can run the application in dev mode that enables live coding using:

```shell script
cd fleet-api
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/fleet-management-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## API Endpoints

The following REST endpoints are available:

- `GET /api/vehicles` - Get all vehicles
- `GET /api/vehicles/{id}` - Get a vehicle by ID
- `POST /api/vehicles` - Create a new vehicle
- `PUT /api/vehicles/{id}` - Update a vehicle
- `DELETE /api/vehicles/{id}` - Delete a vehicle

## Database Management

### Liquibase Migrations

Database migrations are managed by Liquibase and automatically applied when the application starts. The migrations are defined in:

- `fleet-api/src/main/resources/db/changelog/db.changelog-master.xml`
- `fleet-api/src/main/resources/db/changelog/changes/*.xml`

### jOOQ Code Generation

jOOQ code is generated during the build process based on the database schema. The configuration is defined in the `fleet-api/pom.xml` file.
