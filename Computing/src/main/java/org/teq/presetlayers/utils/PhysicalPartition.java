package org.teq.presetlayers.utils;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.util.MathUtils;
import org.teq.presetlayers.PackageBean;

import java.util.HashMap;

public class PhysicalPartition {

    /**
     * 找到指定并行度后，能够使的物理分区为指定分区的正整数
     * 0 3 0
     * 0 5 1
     * 0 8 2
     * @param size
     * @return
     */
    private static HashMap<Integer,Integer> findReferMap(int size) {
        int count = 0;
        HashMap<Integer,Integer> hashMap = new HashMap<>();
        while(hashMap.size() < size){
            int channel = getChannel(count, size);
            if(!hashMap.containsKey(channel)){
                hashMap.put(channel, count);
            }
            count++;
        }
        return hashMap;
    }
    private static int getChannel(Integer n, int para){
        return (MathUtils.murmurHash(n) % 128) * para / 128;
    }
    /**
     * prefix 代表除去数字ID以外的部分
     * size 表示需要分割的分区数目
     * @param prefix
     * @param size
     */
    public static KeyedStream<PackageBean, Integer> partitionByTarget(String prefix, int size, DataStream<PackageBean> dataStream) {
        HashMap<Integer,Integer> referMap = findReferMap(size);
        return dataStream.keyBy(new KeySelector<PackageBean, Integer>() {
            @Override
            public Integer getKey(PackageBean packageBean) throws Exception {
                int id = Integer.parseInt(packageBean.getTarget().substring(prefix.length()));
                return referMap.get(id);
            }
        });
    }

}

