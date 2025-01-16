package org.teq.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.TeqGlobalConfig;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.StaticSerializer;

import java.lang.reflect.Field;
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
            List<String>algorithms = new ArrayList<>();
            algorithms.add("Algorithm1");
            res.type("application/json");
            return JSON.toJSONString(algorithms);
        });
        get("/layer", (req, res) -> {
            List<String>layers = DockerRuntimeData.getLayerList();
            res.type("application/json");
            return JSON.toJSONString(layers);
        });
        get("/node", (request, response) -> {
            String algorithm = request.queryParams("algorithm");
            String layer = request.queryParams("layer");
            if (algorithm == null || layer == null) {
                response.status(400); // 设置 HTTP 状态码为 400
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required parameters: algorithm or layer");
                return JSON.toJSONString(errorResponse);
            }
            var nodes = DockerRuntimeData.getNodeNameListByLayerName(layer);
            response.type("application/json");
            return JSON.toJSONString(nodes);
        });
        get("/config/node", (request, response) -> {
            String algorithm = request.queryParams("algorithm");
            String layer = request.queryParams("layer");
            String nodeName = request.queryParams("node");
            if (algorithm == null || layer == null || nodeName == null) {
                response.status(400);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required parameters: algorithm, layer or node");
                return JSON.toJSONString(errorResponse);
            }
            var nodeParams = parametersList.get(DockerRuntimeData.getNodeIdByName(nodeName));
            response.type("application/json");
            return JSON.toJSONString(nodeParams);
        });
        post("/config/node", (req, res) -> {
            res.type("application/json");
            JSONObject requestBody;
            try {
                requestBody = JSON.parseObject(req.body());
            } catch (Exception e) {
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Invalid JSON format."));
            }
            if (!requestBody.containsKey("name") ||
                    !requestBody.containsKey("key") ||
                    !requestBody.containsKey("value")) {
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Missing required fields: 'name', 'key', and 'value'."));
            }
            String layer = requestBody.getString("layer");
            String name = requestBody.getString("name");
            String key = requestBody.getString("key");
            String newValue = requestBody.getString("value");
            logger.info("Received request to update configuration for node: " + name + ", key: " + key + ", value: " + newValue);
            if(name.equals("All nodes")){
                List<String> nodes = DockerRuntimeData.getNodeNameListByLayerName(layer);
                for(String node:nodes){
                    switch (key) {
                        case "cpuUsageRate":
                            dockerRunner.changeCpuUsageRate(node, Double.parseDouble(newValue));
                            parametersList.get(DockerRuntimeData.getNodeIdByName(node)).setCpuUsageRate(Double.parseDouble(newValue));
                            break;
                        case "memorySize":
                            dockerRunner.changeMemorySize(node, Double.parseDouble(newValue));
                            parametersList.get(DockerRuntimeData.getNodeIdByName(node)).setMemorySize(Double.parseDouble(newValue));
                            break;
                        case "networkOutBandwidth":
                            dockerRunner.changeNetwork(node, Double.parseDouble(newValue), parametersList.get(DockerRuntimeData.getNodeIdByName(node)).getNetworkOutLatency());
                            parametersList.get(DockerRuntimeData.getNodeIdByName(node)).setNetworkOutBandwidth(Double.parseDouble(newValue));
                            break;
                        case "networkOutLatency":
                            dockerRunner.changeNetwork(node, parametersList.get(DockerRuntimeData.getNodeIdByName(node)).getNetworkOutBandwidth(), Double.parseDouble(newValue));
                            parametersList.get(DockerRuntimeData.getNodeIdByName(node)).setNetworkOutLatency(Double.parseDouble(newValue));
                            break;
                        default:
                            res.status(400); // Bad Request
                            return JSON.toJSONString(Map.of("error", "Invalid key: " + key));
                    }
                }
            }
            try {
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
                        dockerRunner.changeNetwork(name, Double.parseDouble(newValue), parametersList.get(DockerRuntimeData.getNodeIdByName(name)).getNetworkOutLatency());
                        parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setNetworkOutBandwidth(Double.parseDouble(newValue));
                        break;
                    case "networkOutLatency":
                        dockerRunner.changeNetwork(name, parametersList.get(DockerRuntimeData.getNodeIdByName(name)).getNetworkOutBandwidth(), Double.parseDouble(newValue));
                        parametersList.get(DockerRuntimeData.getNodeIdByName(name)).setNetworkOutLatency(Double.parseDouble(newValue));
                        break;
                    default:
                        res.status(400); // Bad Request
                        return JSON.toJSONString(Map.of("error", "Invalid key: " + key));
                }
            } catch (Exception e) {
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Invalid value for key: " + key));
            }
            res.status(200); // OK
            return JSON.toJSONString(Map.of("message", "Configuration updated successfully."));
        });
    }

}
