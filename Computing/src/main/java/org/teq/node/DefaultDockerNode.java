package org.teq.node;

import org.apache.http.annotation.Obsolete;

public class DefaultDockerNode extends AbstractDockerNode{
    public DefaultDockerNode() {
        super(new DockerNodeParameters());//default parameters
    }
    public DefaultDockerNode(DockerNodeParameters parameters) {
        super(parameters);
    }

    @Override
    public void process() {
        System.out.println("Starting DefaultDockerNode");
    }
}
