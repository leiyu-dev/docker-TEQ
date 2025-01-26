package example;


import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractWorkerNode;
import org.teq.utils.DockerRuntimeData;
import presetlayers.DevicePrefixName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker extends AbstractWorkerNode {
    private static final Logger logger = LogManager.getLogger(Worker.class);

    /**
     * CenterQuery 格式：
     *  Source: Center
     *  Target: Worker_i
     *  Type: InteriorQuery
     *  Data: new ArrayList<Integer>
     * Center Respond格式：
     * Source: Worker_i
     * Target:  Center
     * Type: InteriorRespond
     * Data: new HashMap<Integer, HashMap<String, Integer>>
     * @param info
     * @return
     */

    //FIXME:keyedStream?
    @Override
    public DataStream<PackageBean> transform(DataStream<PackageBean> info) {
        return info.flatMap(new RichFlatMapFunction<PackageBean, PackageBean>() {
            private Map<String, Map<Long, Integer>> workerState;
            // key 表示单词
            // value 存储对应的时间间隔，比如key = 3000(s), 表示范围 3000000 - 3000999(ms)和对应单词的频数，格式是[timeInterval: frequency]
            private final String[] actionList = {"Left","Right","Forward","Back","Jump","Sit","Slip","Smile","Cry","Help"};

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
//                workerState= getRuntimeContext().getMapState(new MapStateDescriptor<String, Map<Long,Integer>>("workerState", Types.STRING,Types.MAP(Types.LONG, Types.INT)));
                workerState = new HashMap<>();
            }
            @Override
            public void flatMap(PackageBean packageBean, Collector<PackageBean> collector) throws Exception {

                if(packageBean.getType() == InfoType.Data) {
                    updateState(packageBean);
                }
                else if(packageBean.getType() == InfoType.Query) {
                    List<String> object = JSONObject.parseArray(packageBean.getObject().toString(), String.class);
                    String type = object.get(11);
                    if(type.equals("Local")) { //use local data to respond, then send back to Coordinator
                        List<String> ans = respondLocalQuery(object,true);
                        packageBean.setTarget(packageBean.getSrc());
                        packageBean.setType(InfoType.Respond);
                        packageBean.setObject(ans);
                        collector.collect(packageBean);
                    }
                    else if(type.equals("Global")) { // send to Center to get global data
                        packageBean.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.dataCenterLayerName).get(0));
                        collector.collect(packageBean);
                    }
                }
                else if(packageBean.getType() == InfoType.InteriorQuery) { // this stream comes from Center, we need to respond to it
                    List<String> object = JSONObject.parseArray(packageBean.getObject().toString(), String.class);
                    List<String> ans = respondLocalQuery(object, true);
                    packageBean.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.dataCenterLayerName).get(0));
                    packageBean.setType(InfoType.InteriorRespond);
                    packageBean.setObject(ans);
                    collector.collect(packageBean);
                }
                else {// the ans from dataCenter, we just forward it
                    //FIXME: i can't understand this part
                    logger.info(packageBean.toString());
                    packageBean.setTarget(packageBean.getSrc());
                    packageBean.setSrc(DevicePrefixName.Center.getName());
                    collector.collect(packageBean);
                }
            }
            public void updateState(PackageBean packageBean) throws Exception {
                if(workerState.isEmpty()) {
                    // String[] actionList = {"Left","Right","Forward","Back","Jump","Sit","Slip","Smile","Cry","Help"};
                    for (String action: actionList) {
                        workerState.put(action, new HashMap<>());
                    }
                }
                List<String> object = JSONObject.parseArray(packageBean.getObject().toString(), String.class);
                Long second = Long.parseLong(object.get(object.size() - 2)) / 1000;
                String action = "";
                Integer frequency;
                for (int i = 1; i < 11; i++) {
                    if(!(action = object.get(i)).isEmpty()) {
                        Map<Long, Integer> hashMap = workerState.get(action);
                        frequency = hashMap.getOrDefault(second, 0); // 得到当前时间段的频数
                        hashMap.put(second, frequency + 1);
                    }
                }
            }
            // object 指的是LocalQuery负载的内容, addId 这一项用来判断是否在回复的时候在最开头添加Query的ID
            public List<String> respondLocalQuery(List<String> object, boolean addId) throws Exception {
                List<String> ans = new ArrayList<>(); // 偶数位为单词，奇数位为频数，从1开始
                if(addId) ans.add(object.get(0)); // 加入当前查询的Id
                String action = "";
                long start = Long.parseLong(object.get(object.size() - 3)) / 1000;
                long end = Long.parseLong(object.get(object.size() - 2)) / 1000;
                for (int i = 1; i < 11; i++) {
                    if(!(action = object.get(i)).isEmpty()) {
                        Map<Long, Integer> hashMap = workerState.get(action);
                        int sum = 0;
                        for (long j = start; j < end; j++) {
                            //  System.out.println("worker" + (end - start));
                            sum += hashMap.getOrDefault(j, 0);
                        }
                        ans.add(action);
                        ans.add(Integer.toString(sum));
                    }
                }
                return ans;
            }
        });
    }
}