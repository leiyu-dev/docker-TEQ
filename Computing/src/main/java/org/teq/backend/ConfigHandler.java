package org.teq.backend;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.TeqGlobalConfig;
import org.teq.simulator.Simulator;
import org.teq.utils.StaticSerializer;
import org.teq.utils.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;
public class ConfigHandler {
    private static final Logger logger = LogManager.getLogger(ConfigHandler.class);
    List<Class<? extends TeqGlobalConfig>> configs;
    public ConfigHandler(List<Class<? extends TeqGlobalConfig>> configs) {
        this.configs = configs;
    }
    public void handleConfig() {
        get("/config/name", (req, res) -> {
            List<String>names = new ArrayList<>();
            for(Class<? extends TeqGlobalConfig> config : configs){
                names.add(config.getName());
            }
            return JSON.toJSONString(names);
        });
        get("/config/detail", (req, res) -> {
            String name = req.queryParams("name");
            if (name == null || name.isEmpty()) {
                res.status(400);
                return JSON.toJSONString(Map.of("error", "Parameter 'name' is required"));
            }
            try {
                Class clazz = Class.forName(name);
                return StaticSerializer.serializeToJson(clazz);
            } catch (ClassNotFoundException e) {
                res.status(400);
                return JSON.toJSONString(Map.of("error", "Class not found"));
            }
        });
        ObjectMapper objectMapper = new ObjectMapper();
        post("/config", (req, res) -> {
            logger.info("POST /config");
            res.type("application/json");
            // parse request body
            JsonNode requestBody;
            try {
                requestBody = objectMapper.readTree(req.body());
            } catch (Exception e) {
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Invalid JSON format."));
            }

            // validate request body
            if (!requestBody.has("name") || !requestBody.has("key") || !requestBody.has("value")) {
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Missing required fields: 'name', 'key', and 'value'."));
            }

            // get request body fields
            String name = requestBody.get("name").asText();
            String key = requestBody.get("key").asText();
            String newValue = requestBody.get("value").asText();

            // get config class
            try {
                Class clazz = Class.forName(name);
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object convertedValue = StaticSerializer.convertStringToObject(newValue, fieldType);
                field.set(null, convertedValue);
            } catch (Exception e) {
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Class not found."));
            }
            res.status(200); // OK
            return objectMapper.writeValueAsString(Map.of("message", "Configuration updated successfully."));
        });
    }

}
