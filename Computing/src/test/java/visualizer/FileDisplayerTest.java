package visualizer;

import org.apache.logging.log4j.Logger;
import org.teq.visualizer.Chart;
import org.teq.visualizer.FileDisplayer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileDisplayerTest {
    public static void main(String[] args) {
        FileDisplayer fileDisplayer = new FileDisplayer();
        BlockingQueue<Integer>x = new ArrayBlockingQueue<>(100);
        BlockingQueue<Integer>y = new ArrayBlockingQueue<>(100);

        //add data per second
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    x.put(i);
                    y.put(i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Chart chart = new Chart(x,y,"x","y","Test Chart");
        fileDisplayer.addChart(chart);
        fileDisplayer.display();
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
