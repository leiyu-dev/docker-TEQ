package org.teq.node;

import org.apache.http.annotation.Obsolete;

public class DefaultDockerNode extends AbstractDockerNode{
    @Override
    public void process() {
        System.out.println("Starting DefaultDockerNode");
    }
}
