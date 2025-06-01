package com.barbu.fleetmanagement.penaltypoints;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.ws.rs.core.Application;

@QuarkusMain
public class PenaltyPointsCalculatorApp extends Application {

    public static void main(String[] args) {
        Quarkus.run(args);
    }

}