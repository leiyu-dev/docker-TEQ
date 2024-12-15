package org.teq.utils.dataSet.dataSetPlayer;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CommonReader;

import java.util.concurrent.BlockingQueue;

public class CommonDataSource<T> implements SourceFunction<T> {
    private boolean isRunning = true;
    private long minBuffer = 0; // unit: ms, 每发出数据条目之间设置的缓冲时间
    private CommonReader<T> commonReader;
    private static final int cacheSize = 30;

    public CommonDataSource(CommonReader<T> commonReader) {
        this.commonReader = commonReader;
    }

    public CommonDataSource(long minBuffer, CommonReader<T> commonReader) {
        this.minBuffer = minBuffer;
        this.commonReader = commonReader;
    }

    @Override
    public void run(SourceContext<T> sourceContext) throws Exception {
        BlockingQueue<T> blockingQueue = commonReader.getBlockingQueue();
        Thread reader = new Thread(commonReader);
        reader.start();
        while(isRunning){
            sourceContext.collect(blockingQueue.take());
            if(minBuffer > 0){
                Thread.sleep(minBuffer);
            }
        }
    }
    @Override
    public void cancel() {
        isRunning = false;
    }
}
