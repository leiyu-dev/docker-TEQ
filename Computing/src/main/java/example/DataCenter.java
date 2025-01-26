package example;


import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.exception.DockerException;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.util.Collector;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractDataCenterNode;
import org.teq.utils.DockerRuntimeData;

import java.util.*;

public class DataCenter extends AbstractDataCenterNode {

    /**
     * CenterQuery 格式：
     *  Source: Center
     *  Target: Worker_i
     *  Type: InteriorQuery
     *  Data: new ArrayList<Integer>
     * Center Respond格式：
     * Source: Worker_i
     * Target:  Center
     * Data: new HashMap<Integer, HashMap<String, Integer>>
     * @param info
     * @return
     */

    @Override
    public DataStream<PackageBean> transform(DataStream<PackageBean> info) {
        int workerNum = DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.workerLayerName).size();
        // 如果要使用状态编程，必须将流转化为KeyedStream!!!!
        KeyedStream<PackageBean, String> centerStream = info.keyBy(new KeySelector<PackageBean, String>() {
            @Override
            public String getKey(PackageBean brokerBean) throws Exception {
                return brokerBean.getTarget();
            }
        });
        return centerStream.flatMap(new RichFlatMapFunction<PackageBean, PackageBean>() {
            private MapState<Integer, Map<String, Integer>> centerState; // 存入收到的来自worker 的 local query respond,key 为 query id
            private MapState<Integer, PackageBean> querySrc; // 存入global query的src端，同样也是respond的回复端
            private MapState<Integer,Integer > queryState; // 存入global query的目前回答的状态，如果value = workerNum, 那么可以回答这个query
            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                centerState = getRuntimeContext().getMapState(new MapStateDescriptor<Integer, Map<String,Integer>>("centerState", Types.INT,Types.MAP(Types.STRING, Types.INT)));
                querySrc = getRuntimeContext().getMapState(new MapStateDescriptor<Integer, PackageBean>("querySrc", Integer.class, PackageBean.class));
                queryState = getRuntimeContext().getMapState(new MapStateDescriptor<Integer, Integer>("queryState", Types.INT, Types.INT));
            }
            @Override
            public void flatMap(PackageBean packageBean, Collector<PackageBean> collector) throws Exception {
                if(packageBean.getType() == InfoType.Query){ // global query, ask worker to do local query
                    List<String> object = JSONObject.parseArray(packageBean.getObject().toString(), String.class);
                    int id = Integer.parseInt(object.get(0));
                    centerState.put(id, new LinkedHashMap<>());
                    querySrc.put(id, packageBean);
                    queryState.put(id,0);
                    for (int i = 0; i < workerNum; i++) {
                        collector.collect(new PackageBean(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.dataCenterLayerName).get(0),
                                DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.workerLayerName).get(i),
                                InfoType.InteriorQuery, packageBean.getObject()));
                    }
                }
                else if(packageBean.getType() == InfoType.InteriorRespond) { // worker respond to global query
                    List<String> object = JSONObject.parseArray(packageBean.getObject().toString(), String.class);
                    int id = Integer.parseInt(object.get(0));
                    updateCenterState(object,id);
                    if(queryState.get(id) == workerNum) {
                        PackageBean p = querySrc.get(id);
                        List<String> ans = new ArrayList<>();
                        ans.add(Integer.toString(id));
                        Map<String, Integer> ansSet = centerState.get(id);
                        for(String word : ansSet.keySet()) {
                            ans.add(word);
                            ans.add(Integer.toString(ansSet.get(word)));
                        }
                        Random random = new Random();
                        collector.collect(new PackageBean(p.getId(), p.getSrc(), DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.dataCenterLayerName).get(random.nextInt(workerNum)),
                              ExecutorParameters.fromCenterToWorkerPort  , InfoType.Respond, ans));
                    }
                }
            }
            public void updateCenterState(List<String> object, int id) throws Exception {
                Map<String, Integer> ansSet = centerState.get(id);
                for (int i = 1; i < object.size(); ) {
                    String action = object.get(i++);
                    Integer freq = Integer.parseInt(object.get(i++));
                    Integer oldFreq = ansSet.getOrDefault(action, 0);
                    ansSet.put(action, freq + oldFreq);
                }
                queryState.put(id, queryState.get(id) + 1);
            }
        }
        ).setParallelism(1);
    }
}
