package visualizer;

import org.teq.node.AbstractDockerNode;

public class RunningNode extends AbstractDockerNode {
    @Override
    public void process() {
        while(true){
            System.out.println("RunningNode is running");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
