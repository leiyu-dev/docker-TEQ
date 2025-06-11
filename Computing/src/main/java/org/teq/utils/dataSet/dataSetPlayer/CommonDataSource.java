package org.teq.utils.dataSet.dataSetPlayer;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.teq.configurator.ExecutorConfig;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CommonReader;

import java.util.concurrent.BlockingQueue;

public class CommonDataSource<T> implements SourceFunction<T> {
    private boolean isRunning = true;
    private CommonReader<T> commonReader;
    private static final int cacheSize = 30;

    public CommonDataSource(CommonReader<T> commonReader) {
        this.commonReader = commonReader;
    }

    public CommonDataSource(long minBuffer, CommonReader<T> commonReader) {
        ExecutorConfig.minBuffer = minBuffer;
        this.commonReader = commonReader;
    }

    @Override
    public void run(SourceContext<T> sourceContext) throws Exception {
        BlockingQueue<T> blockingQueue = commonReader.getBlockingQueue();
        Thread reader = new Thread(commonReader);
        reader.start();
        while(isRunning){
            sourceContext.collect(blockingQueue.take());
            if(ExecutorConfig.minBuffer > 0){
                Thread.sleep(ExecutorConfig.minBuffer);
            }
        }
    }
    @Override
    public void cancel() {
        isRunning = false;
    }
}
