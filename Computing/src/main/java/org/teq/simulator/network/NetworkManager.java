package org.teq.simulator.network;

import java.util.List;
import java.util.Map;
import java.util.Set;


//TODO:用set存储，不用map，自动分配端口，上层可以获取端口，这里也需要一个addNode方法
/* this class is used to manage the network between different nodes
 *
 */
public class NetworkManager {
    private Map<String,Integer>portMap;
    private Map<Integer, Map<Integer,Double>>networkMap;

    /* @param nodeFrom: the node from which the connection is to be made
     * @param nodeTo: the node to which the connection is to be made
     * @return: true if the connection is made successfully, false otherwise
     */
    boolean AddConnect(int nodeFrom,int nodeTo,double bandWidth){
        Map<Integer,Double> nodeMap = networkMap.get(nodeFrom);
        if(nodeMap.containsKey(nodeTo)){
            return false;
        }
        nodeMap.put(nodeTo,bandWidth);
        return true;
    }

    /* @param nodeFrom: the node name from which the connection is to be made
     * @param nodeTo: the node name to which the connection is to be made
     * @return: true if the connection is made successfully, false otherwise
     */
    boolean AddConnect(String nodeFrom,String nodeTo,double bandWidth){
        int nodeFromIndex = portMap.get(nodeFrom);
        int nodeToIndex = portMap.get(nodeTo);
        return AddConnect(nodeFromIndex,nodeToIndex,bandWidth);
    }

    /* @param nodeFrom: the node from which the connection is to be removed
     * @param nodeTo: the node to which the connection is to be removed
     * @return: true if the connection is removed successfully, false otherwise
     */
    boolean RemoveConnect(int nodeFrom,int nodeTo){
        Map<Integer,Double> nodeMap = networkMap.get(nodeFrom);
        if(!nodeMap.containsKey(nodeTo)){
            return false;
        }
        nodeMap.remove(nodeTo);
        return true;
    }

    /* @param nodeFrom: the node name from which the connection is to be removed
     * @param nodeTo: the node name to which the connection is to be removed
     * @return: true if the connection is removed successfully, false otherwise
     */
    boolean RemoveConnect(String nodeFrom,String nodeTo){
        int nodeFromIndex = portMap.get(nodeFrom);
        int nodeToIndex = portMap.get(nodeTo);
        return RemoveConnect(nodeFromIndex,nodeToIndex);
    }

    /* @param nodeFrom: the node from which the connection is to be updated
     * @param nodeTo: the node to which the connection is to be updated
     * @return: true if the connection is updated successfully, false otherwise
     */
    boolean UpdateConnect(int nodeFrom,int nodeTo,double bandWidth){
        Map<Integer,Double> nodeMap = networkMap.get(nodeFrom);
        if(!nodeMap.containsKey(nodeTo)){
            return false;
        }
        nodeMap.put(nodeTo,bandWidth);
        return true;
    }

    /* @param nodeFrom: the node name from which the connection is to be updated
     * @param nodeTo: the node name to which the connection is to be updated
     * @return: true if the connection is updated successfully, false otherwise
     */
    boolean UpdateConnect(String nodeFrom,String nodeTo,double bandWidth){
        int nodeFromIndex = portMap.get(nodeFrom);
        int nodeToIndex = portMap.get(nodeTo);
        return UpdateConnect(nodeFromIndex,nodeToIndex,bandWidth);
    }
}
