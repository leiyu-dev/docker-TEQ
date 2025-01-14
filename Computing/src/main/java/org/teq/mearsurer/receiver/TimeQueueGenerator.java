package org.teq.mearsurer.receiver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TimeQueueGenerator {
    private static BlockingQueue<Double> timeQueue;

    static private BlockingQueue<Double> getTimeQueue(int intervalMilliseconds) {
        BlockingQueue<Double> timeQueue = new ArrayBlockingQueue<>(100);
        Thread threadTime = new Thread(new Thread(){
            double time = 0;
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(intervalMilliseconds);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    time+=intervalMilliseconds/1000.0;
                    try {
                        timeQueue.put(time);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        threadTime.start();
        return timeQueue;
    }
}
