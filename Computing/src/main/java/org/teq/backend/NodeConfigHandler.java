package org.teq.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class NodeConfigHandler {
    private static final Logger logger = LogManager.getLogger(NodeConfigHandler.class);
    public DockerRunner dockerRunner;
    List<DockerNodeParameters> parametersList;
    public NodeConfigHandler(List<DockerNodeParameters> parametersList,DockerRunner dockerRunner) {
        this.dockerRunner = dockerRunner;
        this.parametersList = parametersList;
    }

    public void handleNodeConfig(){
        get("/algorithm", (req, res) -> {
            logger.debug("API Request: GET /algorithm - Retrieving available algorithms");
            List<String>algorithms = new ArrayList<>();
            algorithms.add("Algorithm1");
            res.type("application/json");
            logger.debug("API Response: GET /algorithm - Returned {} algorithms", algorithms.size());
            return JSON.toJSONString(algorithms);
        });
        get("/layer", (req, res) -> {
            logger.debug("API Request: GET /layer - Retrieving available layers");
            List<String>layers = DockerRuntimeData.getLayerList();
            res.type("application/json");
            logger.debug("API Response: GET /layer - Returned {} layers", layers.size());
            return JSON.toJSONString(layers);
        });
        get("/node", (request, response) -> {
            String algorithm = request.queryParams("algorithm");
            String layer = request.queryParams("layer");
            logger.debug("API Request: GET /node - Retrieving nodes for algorithm={}, layer={}", algorithm, layer);
            if (algorithm == null || layer == null) {
                logger.warn("API Warning: GET /node - Missing required parameters: algorithm={}, layer={}", algorithm, layer);
                response.status(400); // 设置 HTTP 状态码为 400
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required parameters: algorithm or layer");
                return JSON.toJSONString(errorResponse);
            }
            try {
                var nodes = DockerRuntimeData.getNodeNameListByLayerName(layer);
                response.type("application/json");
                logger.debug("API Response: GET /node - Returned {} nodes for layer {}", nodes.size(), layer);
                return JSON.toJSONString(nodes);
            } catch (Exception e) {
                logger.error("API Error: GET /node - Failed to retrieve nodes for layer: {}", layer, e);
                response.status(500);
                return JSON.toJSONString(Map.of("error", "Failed to retrieve nodes: " + e.getMessage()));
            }
        });
        get("/config/node", (request, response) -> {
            String algorithm = request.queryParams("algorithm");
            String layer = request.queryParams("layer");
            String nodeName = request.queryParams("node");
            logger.debug("API Request: GET /config/node - Retrieving config for algorithm={}, layer={}, node={}", algorithm, layer, nodeName);
            if (algorithm == null || layer == null || nodeName == null) {
                logger.warn("API Warning: GET /config/node - Missing required parameters: algorithm={}, layer={}, node={}", algorithm, layer, nodeName);
                response.status(400);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required parameters: algorithm, layer or node");
                return JSON.toJSONString(errorResponse);
            }
            try {
                var nodeParams = parametersList.get(DockerRuntimeData.getNodeIdByName(nodeName));
                response.type("application/json");
                logger.debug("API Response: GET /config/node - Retrieved configuration for node: {}", nodeName);
                return JSON.toJSONString(nodeParams);
            } catch (Exception e) {
                logger.error("API Error: GET /config/node - Failed to retrieve config for node: {}", nodeName, e);
                response.status(500);
                return JSON.toJSONString(Map.of("error", "Failed to retrieve node configuration: " + e.getMessage()));
            }
        });
        post("/config/node", (req, res) -> {
            logger.info("API Request: POST /config/node - Updating node configuration");
            logger.debug("API Request: POST /config/node - Request body: {}", req.body());
            res.type("application/json");
            JSONObject requestBody;
            try {
                requestBody = JSON.parseObject(req.body());
            } catch (Exception e) {
                logger.error("API Error: POST /config/node - Invalid JSON format in request body", e);
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Invalid JSON format."));
            }
            if (!requestBody.containsKey("name") ||
                    !requestBody.containsKey("key") ||
                    !requestBody.containsKey("value")) {
                logger.warn("API Warning: POST /config/node - Missing required fields in request");
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Missing required fields: 'name', 'key', and 'value'."));
            }
            String layer = requestBody.getString("layer");
            String name = requestBody.getString("name");
            String key = requestBody.getString("key");
            String newValue = requestBody.getString("value");
            logger.info("API Processing: POST /config/node - Updating config for node={}, key={}, value={}", name, key, newValue);
            if(name.equals("All nodes")){
                try {
                    List<String> nodes = DockerRuntimeData.getNodeNameListByLayerName(layer);
                    logger.info("API Processing: POST /config/node - Updating {} nodes in layer: {}", nodes.size(), layer);
                    for (String node : nodes) {
                        changeParameter(node, key, newValue);
                    }
                    logger.info("API Success: POST /config/node - Successfully updated all nodes in layer: {}", layer);
                } catch (Exception e) {
                    logger.error("API Error: POST /config/node - Failed to update all nodes in layer: {}", layer, e);
                    res.status(400); // Bad Request
                    return JSON.toJSONString(Map.of("error", e.getMessage()));
                }
            }
            else try {
                changeParameter(name, key, newValue);
                logger.info("API Success: POST /config/node - Successfully updated node: {}", name);
            } catch (Exception e) {
                logger.error("API Error: POST /config/node - Failed to update node: {}", name, e);
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", e.getMessage()));
            }
            res.status(200); // OK
            return JSON.toJSONString(Map.of("message", "Configuration updated successfully."));
        });
    }
    private void changeParameter(String name, String key, String newValue) {
        logger.debug("Changing parameter for node: {}, key: {}, value: {}", name, key, newValue);
        switch (key) {
            case "cpuUsageRate":
                dockerRunner.changeCpuUsageRate(name, Double.parseDouble(newValue));
                parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setCpuUsageRate(Double.parseDouble(newValue));
                break;
            case "memorySize":
                dockerRunner.changeMemorySize(name, Double.parseDouble(newValue));
                parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setMemorySize(Double.parseDouble(newValue));
                break;
            case "networkOutBandwidth":
                dockerRunner.changeNetworkOut(name, Double.parseDouble(newValue), parametersList.get(DockerRuntimeData.getNodeIdByName(name)).getNetworkOutLatency());
                parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setNetworkOutBandwidth(Double.parseDouble(newValue));
                break;
            case "networkOutLatency":
                dockerRunner.changeNetworkOut(name, parametersList.get(DockerRuntimeData.getNodeIdByName(name)).getNetworkOutBandwidth(), Double.parseDouble(newValue));
                parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setNetworkOutLatency(Double.parseDouble(newValue));
                break;
            case "networkInBandwidth":
                dockerRunner.changeNetworkIn(name, Double.parseDouble(newValue), parametersList.get(DockerRuntimeData.getNodeIdByName(name)).getNetworkInLatency());
                parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setNetworkInBandwidth(Double.parseDouble(newValue));
                break;
            case "networkInLatency":
                dockerRunner.changeNetworkIn(name, parametersList.get(DockerRuntimeData.getNodeIdByName(name)).getNetworkInBandwidth(), Double.parseDouble(newValue));
                parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setNetworkInLatency(Double.parseDouble(newValue));
                break;
            default:
                logger.error("Invalid configuration key: {}", key);
                throw new IllegalArgumentException("Invalid key: " + key);
        }
        logger.debug("Successfully changed parameter for node: {}, key: {}, value: {}", name, key, newValue);
    }

}
