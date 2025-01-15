package visualizer;

import org.apache.logging.log4j.Logger;
import org.teq.visualizer.Chart;
import org.teq.visualizer.FileDisplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileDisplayerTest {
    public static void main(String[] args) {
        FileDisplayer fileDisplayer = new FileDisplayer();
        BlockingQueue<Integer>x = new ArrayBlockingQueue<>(100);
        List<BlockingQueue<Integer>> y = new ArrayList<>();
        List<String>yLableList = new ArrayList<>();

        for(int i=1;i<=10;i++){
            y.add(new ArrayBlockingQueue<>(100));
            yLableList.add("label" + i);
        }
        //add data per second
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    x.put(i);
                    for(int j=1;j<=10;j++) {
                        y.get(j).put(j);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Chart chart = new Chart<Integer,Integer>(x,y,"x","y",yLableList,"Test Chart","overview");
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
