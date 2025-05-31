package com.barbu.fleetmanagement.manager;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class IntegrationTestResource  implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> postgreSQLContainer;

    @Override
    public Map<String, String> start() {
        postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres").withTag("17.4"));
        postgreSQLContainer.start();
        Map<String, String> conf = new HashMap<>();
        conf.put("quarkus.datasource.jdbc.url", postgreSQLContainer.getJdbcUrl());
        conf.put("quarkus.datasource.username", postgreSQLContainer.getUsername());
        conf.put("quarkus.datasource.password", postgreSQLContainer.getPassword());
        return conf;
    }

    @Override
    public void stop() {
        postgreSQLContainer.stop();
    }
}
