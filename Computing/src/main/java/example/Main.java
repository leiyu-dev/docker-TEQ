package example;

import org.teq.simulator.Simulator;
public class Main {
    public static void main(String[] args) throws Exception {
        new Thread(new ListenThread()).start();
        Simulator simulator = new Simulator();
        for(int i=1;i<=10;i++){
            simulator.addNode(new MyFlinkNode());
        }
        simulator.start();
    }
}