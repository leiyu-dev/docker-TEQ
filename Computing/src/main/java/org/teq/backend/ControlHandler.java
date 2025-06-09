package org.teq.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.simulator.Simulator;
import org.teq.visualizer.Chart;

import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static spark.Spark.post;

public class ControlHandler {
    private static final Logger logger = LogManager.getLogger(ControlHandler.class);
    Simulator simulator;
    public ControlHandler(Simulator simulator) {
        this.simulator = simulator;
    }
    public void handleControl(){
        post("/start", (req, res) -> {
            logger.info("API Request: POST /start - Starting simulator");
            try {
                simulator.restart();
                res.status(200);
                res.type("application/json");
                logger.info("API Success: POST /start - Simulator started successfully");
                return JSON.toJSONString(Map.of("code", 0));
            } catch (Exception e) {
                logger.error("API Error: POST /start - Failed to start simulator", e);
                res.status(500);
                return JSON.toJSONString(Map.of("code", -1, "error", e.getMessage()));
            }
        });
        post("/stop", (req, res) -> {
            logger.info("API Request: POST /stop - Stopping simulator");
            try {
                simulator.stop();
                res.status(200);
                res.type("application/json");
                logger.info("API Success: POST /stop - Simulator stopped successfully");
                return JSON.toJSONString(Map.of("code", 0));
            } catch (Exception e) {
                logger.error("API Error: POST /stop - Failed to stop simulator", e);
                res.status(500);
                return JSON.toJSONString(Map.of("code", -1, "error", e.getMessage()));
            }
        });
        post("/restart", (req, res) -> {
            logger.info("API Request: POST /restart - Restarting simulator");
            try {
                simulator.restart();
                res.status(200);
                res.type("application/json");
                logger.info("API Success: POST /restart - Simulator restarted successfully");
                return JSON.toJSONString(Map.of("code", 0));
            } catch (Exception e) {
                logger.error("API Error: POST /restart - Failed to restart simulator", e);
                res.status(500);
                return JSON.toJSONString(Map.of("code", -1, "error", e.getMessage()));
            }
        });

    }
}
