package com.barbu.fleetmanagement.manager;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class IntegrationTestResource implements QuarkusTestResourceLifecycleManager {

    private final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres")
                    .withTag("17.4")
    );
    private final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.9.0");


    @Override
    public Map<String, String> start() {
        postgreSQLContainer.start();
        kafka.start();
        Map<String, String> conf = new HashMap<>();
        conf.put("quarkus.datasource.jdbc.url", postgreSQLContainer.getJdbcUrl());
        conf.put("quarkus.datasource.username", postgreSQLContainer.getUsername());
        conf.put("quarkus.datasource.password", postgreSQLContainer.getPassword());
        conf.put("kafka.bootstrap.servers", kafka.getBootstrapServers());
        return conf;
    }

    @Override
    public void stop() {
        postgreSQLContainer.stop();
        kafka.stop();
    }
}
