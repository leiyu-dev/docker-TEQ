package example;

import org.teq.simulator.Simulator;
public class Main {
    public static void main(String[] args) throws Exception {
        Simulator simulator = new Simulator();
        for(int i=1;i<=10;i++){
            simulator.addNode(new MyFlinkNode());
        }
        simulator.start();
    }
}