package example;

import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.Simulator;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

class mynode extends AbstractFlinkNode{
        public void process(){
            System.out.println("I DO PROCESS 1");
            try {
                runEnvironment();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //do something
        }
}

public class Main {
    public static void main(String[] args) {
        Simulator simulator = new Simulator();
        simulator.addNode(mynode.class);
        simulator.start();
//        simulator.addNode(new AbstractFlinkNode(){
//            @Override
//            public void process(){
//                System.out.println("I DO PROCESS 2");
//                //do other thing
//            }
//        });
//        simulator.start();
    }
}