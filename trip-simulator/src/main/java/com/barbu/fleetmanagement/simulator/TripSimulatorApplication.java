package com.barbu.fleetmanagement.simulator;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class TripSimulatorApplication {

    public static void main(String... args) {
        Quarkus.run(args);
    }
}