package example;

public enum DevicePrefixName {
    Center("center"),
    Coordinator("coordinator"),
    Worker("worker"),
    Sensor("sensor");
    private final String name;
    DevicePrefixName(String i) {this.name = i;}
    public String getName(){return this.name;}
}
