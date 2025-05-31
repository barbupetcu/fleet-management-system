package com.barbu.fleetmanagement.manager;

import jakarta.ws.rs.core.Application;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.Quarkus;

@QuarkusMain
public class FleetManagerApplication extends Application {

    public static void main(String[] args) {
        Quarkus.run(args);
    }
}