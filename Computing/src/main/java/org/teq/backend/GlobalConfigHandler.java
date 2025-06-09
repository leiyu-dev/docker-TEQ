package org.teq.backend;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.TeqGlobalConfig;
import org.teq.utils.StaticSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;
public class GlobalConfigHandler {
    private static final Logger logger = LogManager.getLogger(GlobalConfigHandler.class);
    List<Class<? extends TeqGlobalConfig>> configs;
    public GlobalConfigHandler(List<Class<? extends TeqGlobalConfig>> configs) {
        this.configs = configs;
    }
    public void handleGlobalConfig() {
        get("/config/name", (req, res) -> {
            logger.debug("API Request: GET /config/name - Retrieving configuration class names");
            List<String>names = new ArrayList<>();
            for(Class<? extends TeqGlobalConfig> config : configs){
                names.add(config.getName());
            }
            logger.debug("API Response: GET /config/name - Returned {} configuration classes", names.size());
            return JSON.toJSONString(names);
        });
        get("/config/detail", (req, res) -> {
            String name = req.queryParams("name");
            logger.debug("API Request: GET /config/detail - Retrieving details for config class: {}", name);
            if (name == null || name.isEmpty()) {
                logger.warn("API Warning: GET /config/detail - Missing required parameter: name");
                res.status(400);
                return JSON.toJSONString(Map.of("error", "Parameter 'name' is required"));
            }
            try {
                Class clazz = Class.forName(name);
                String result = StaticSerializer.serializeToJson(clazz);
                logger.debug("API Success: GET /config/detail - Retrieved configuration details for class: {}", name);
                return result;
            } catch (ClassNotFoundException e) {
                logger.error("API Error: GET /config/detail - Class not found: {}", name, e);
                res.status(400);
                return JSON.toJSONString(Map.of("error", "Class not found"));
            } catch (Exception e) {
                logger.error("API Error: GET /config/detail - Failed to serialize config class: {}", name, e);
                res.status(500);
                return JSON.toJSONString(Map.of("error", "Failed to retrieve configuration details"));
            }
        });
        ObjectMapper objectMapper = new ObjectMapper();
        post("/config", (req, res) -> {
            logger.info("API Request: POST /config - Updating global configuration");
            logger.debug("API Request: POST /config - Request body: {}", req.body());
            res.type("application/json");
            // parse request body
            JsonNode requestBody;
            try {
                requestBody = objectMapper.readTree(req.body());
            } catch (Exception e) {
                logger.error("API Error: POST /config - Invalid JSON format in request body", e);
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Invalid JSON format."));
            }

            // validate request body
            if (!requestBody.has("name") || !requestBody.has("key") || !requestBody.has("value")) {
                logger.warn("API Warning: POST /config - Missing required fields in request");
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Missing required fields: 'name', 'key', and 'value'."));
            }

            // get request body fields
            String name = requestBody.get("name").asText();
            String key = requestBody.get("key").asText();
            String newValue = requestBody.get("value").asText();

            logger.info("API Processing: POST /config - Updating config: class={}, key={}, value={}", name, key, newValue);

            // get config class
            try {
                Class clazz = Class.forName(name);
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object convertedValue = StaticSerializer.convertStringToObject(newValue, fieldType);
                field.set(null, convertedValue);
                logger.info("API Success: POST /config - Successfully updated configuration: class={}, key={}, value={}", name, key, newValue);
            } catch (ClassNotFoundException e) {
                logger.error("API Error: POST /config - Class not found: {}", name, e);
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Class not found: " + name));
            } catch (NoSuchFieldException e) {
                logger.error("API Error: POST /config - Field not found: {} in class {}", key, name, e);
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Field not found: " + key));
            } catch (Exception e) {
                logger.error("API Error: POST /config - Failed to update configuration: class={}, key={}", name, key, e);
                res.status(400); // Bad Request
                return objectMapper.writeValueAsString(Map.of("error", "Failed to update configuration: " + e.getMessage()));
            }
            res.status(200); // OK
            return objectMapper.writeValueAsString(Map.of("message", "Configuration updated successfully."));
        });
    }

}
