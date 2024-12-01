package org.teq.configurator;

public interface InDockerConfig {
    public void getFromProperties(String configFile);
    public void saveToProperties(String configFile);
}
