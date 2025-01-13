package org.teq.backend;

import org.teq.configurator.SimulatorConfigurator;
import org.teq.simulator.Simulator;

import static spark.Spark.*;

public class BackendManager {
    Simulator simulator;
    public BackendManager(Simulator simulator) {
        this.simulator = simulator;
    }
    public void launch() {
        port(SimulatorConfigurator.restfulPort);
        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        ConfigHandler configHandler = new ConfigHandler(simulator.getConfigs());
        configHandler.handleConfig();
    }
}
