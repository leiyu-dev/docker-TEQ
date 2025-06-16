# Teq: An Open and Developer-friendly Testbed for Edge-based Query Processing Algorithms

Teq is a testbed specifically designed for edge-based query processing algorithms, providing an open, extensible simulation and algorithm deployment environment. It is developer-friendly, simplifying the otherwise cumbersome simulation, complex algorithm module management, and tedious evaluation processes.

## 📦 Quick Start

### Requirements
- Java 11(Higher Java versions are temporarily not supported.)
- Maven 3.6+
- Docker
- Node.js 22+ (for frontend development)

### Installation

1. **Clone the project**
   ```bash
   git clone https://github.com/leiyu-dev/docker-TEQ.git
   cd docker-TEQ
   ```

2. **Pull docker image**
   ```bash
   docker pull leiyu0503/teq:1.0
   ```

3. **start backend project**
   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="example.Example" -pl Computing
   # or you can execute the run.sh script
   ```

4. **Launch Web interface** (optional)
   ```bash
   cd front/visualizer
   npm install
   npm run dev
   ```
if you launch the web interface on the server, you also need to forward port 8889(backend port, can be configured in the configurator) to your local machine.
Web interface will be integrated into the java application in the near future.

## 🚀 Algorithm Implementation Examples

### Simulation Example

```java
public class Example {
    public static void main(String[] args) {
        // create node parameters
        DockerNodeParameters param = new DockerNodeParameters();
        param.setCpuUsageRate(0.5);
        
        // different type of node for different layers
        EndDevice endDevice = new EndDevice();
        Coordinator coordinator = new Coordinator();
        Worker worker = new Worker();
        DataCenter dataCenter = new DataCenter();
        
        // layered model
        Layer endDeviceLayer = new Layer(endDevice, 300, "EndDeviceLayer");
        Layer coordinatorLayer = new Layer(coordinator, 1, "CoordinatorLayer");
        Layer workerLayer = new Layer(worker, 3, "WorkerLayer");
        Layer dataCenterLayer = new Layer(dataCenter, 1, "DataCenterLayer");
        
        // start the simulator
        Simulator simulator = new Simulator(new Network());
        simulator.addLayer(endDeviceLayer);
        simulator.addLayer(coordinatorLayer);
        simulator.addLayer(workerLayer);
        simulator.addLayer(dataCenterLayer);
        
        simulator.start();
    }
}
```

```java
// End Device Layer Implementation
public class EndDevice extends AbstractEndDeviceNode {
    @Override
    public void process(Object input, Object output, String pipe) {
        if (pipe == null) {
            // Send data/query via E2C pipe
            emit(data, "E2C");
        }
        if (pipe.equals("C2E")) {
            // Display query results
            displayResults(input);
        }
    }
}

// Worker Layer Implementation  
public class Worker extends AbstractWorkerNode {
    @Override
    public void process(Object input, Object output, String pipe) {
        if (pipe.equals("C2W")) {
            // Local Top-k finding
            List<Item> localTopK = findLocalTopK(input);
            send(localTopK, "W2C");
        }
    }
}

// Coordinator Layer Implementation
public class Coordinator extends AbstractCoordinatorNode {
    @Override  
    public void process(Object input, Object output, String pipe) {
        if (pipe.equals("E2C")) {
            // Map data/query to workers
            mapToWorkers(input, "C2W");
        }
        if (pipe.equals("W2C")) {
            // Map results back to end devices
            mapToEndDevices(input, "C2E");
        }
    }
}
```

### Custom Flink Processing Node

```java
public class CustomFlinkNode extends AbstractFlinkNode {
    @Override
    public void flinkProcess() {
        StreamExecutionEnvironment env = getEnv();
        DataStream<String> dataStream = env.readTextFile("./data.txt");
        
        // Custom data processing logic
        DataStream<ProcessedData> processedStream = dataStream
            .map(new MapFunction<String, ProcessedData>() {
                @Override
                public ProcessedData map(String value) throws Exception {
                    // Process individual data item
                    return processDataItem(value);
                }
            });
            
        processedStream.print();
    }
}
```

## 🤝 Core Innovations
1. **Streamlined Framework**: Implementing decentralized algorithms as modular, Docker-based executables
2. **Playback Control**: Data and query playback control for reproducible and consistent evaluations  
3. **Metric Transformation**: Converting runtime metrics into system-wide metrics desired by algorithm developers

### Publication Information
- **Conference**: SIGMOD2025, Berlin, Germany

## 🔗 Related Resources

- **Project Homepage**: https://sudis-zju.github.io/teq
- **Technical Report**: Detailed extended technical report
- **Demo Video**: Complete system demonstration and tutorials
- **Code Artifacts**: Reproducible experimental code and data

## 📞 Contact

### Research Team
- **Yu Lei** (Zhejiang University): zjucactus@zju.edu.cn  
- **Huan Li** (Zhejiang University): lihuan.cs@zju.edu.cn

### Technical Support
- Project Issues: [GitHub Issues](https://github.com/sudis-zju/teq/issues)
- Project Docs: [Github](https://sudis-zju.github.io/teq/)
- Technical Discussion: Contact research team through project homepage
