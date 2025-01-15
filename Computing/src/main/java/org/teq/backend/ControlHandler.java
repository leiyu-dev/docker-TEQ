package org.teq.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
    Simulator simulator;
    public ControlHandler(Simulator simulator) {
        this.simulator = simulator;
    }
    public void handleControl(){
        post("/start", (req, res) -> {
            simulator.restart();
            res.status(200);
            res.type("application/json");
            return JSON.toJSONString(Map.of("code", 0));
        });
        post("/stop", (req, res) -> {
            simulator.stop();
            res.status(200);
            res.type("application/json");
            return JSON.toJSONString(Map.of("code", 0));
        });
        post("/restart", (req, res) -> {
            simulator.restart();
            res.status(200);
            res.type("application/json");
            return JSON.toJSONString(Map.of("code", 0));
        });

    }
}
