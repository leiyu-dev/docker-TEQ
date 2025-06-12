# Teq: An Open and Developer-friendly Testbed for Edge-based Query Processing Algorithms

Teq is a testbed specifically designed for edge-based query processing algorithms, providing an open, extensible simulation and algorithm deployment environment. It is developer-friendly, simplifying the otherwise cumbersome simulation, complex algorithm module management, and tedious evaluation processes.

## Key Features

- **Real Query Execution**: Unlike existing simulators that only support resource management, Teq enables the execution and testing of real query processing algorithms
- **Layered Edge Architecture**: Employs a four-layer model (End Device, Coordinator, Worker, Data Center layers) to simulate heterogeneous edge computing environments
- **Docker Containerization**: Provides node isolation and modular deployment based on Docker technology
- **Data Playback Control**: Offers reproducible data and query stream control to ensure consistent and fair algorithm evaluation
- **Real-time Monitoring & Visualization**: Built-in performance metrics monitoring system with runtime metric collection and real-time visualization
- **Developer-friendly**: Simplifies distributed algorithm module implementation - developers only need to implement abstract methods without handling underlying network communications

## 🏗️ System Architecture

### Four-layer Edge Computing Model
```
Data Center Layer    # Global data aggregation and processing
        ↕ D2W / W2D
Worker Layer         # Local computational task execution  
        ↕ W2C / C2W
Coordinator Layer    # Data and query routing
        ↕ C2E / E2C
End Device Layer     # Data generation and query initiation
```

### Backend Architecture (Computing/)
```
org.teq/
├── backend/          # Backend manager and log handling
├── configurator/     # Simulation configuration management
├── layer/            # Layered architecture implementation
├── measurer/         # Performance monitoring and metric collection
│   └── receiver/     # Metric reception and processing
├── node/             # Node abstraction and Docker implementation
├── presetlayers/     # Preset layers and task interfaces
├── simulator/        # Simulator core and Docker runner
│   ├── docker/       # Docker container management
│   └── network/      # Network host nodes
├── utils/            # Utilities and data connectors
│   ├── connector/    # Flink and Netty connectors
│   └── dataSet/      # Dataset players
└── visualizer/       # Visualization components
```

### Frontend Architecture (front/)
- **Controller**: Basic execution controls (start, stop, restart) and runtime information display
- **Configurator**: Runtime settings panel with real-time parameter adjustment support
- **Visualizer**: Interactive chart analysis and real-time data display

## 🔬 Academic Research Background

Teq addresses three core challenges in edge query processing algorithm development and evaluation:

1. **Cumbersome Simulation**: Existing edge computing simulators cannot execute real queries, forcing researchers to integrate simulation code into algorithm implementations
2. **Complex Algorithm Module Management**: Edge computing algorithms contain modules distributed across different nodes, making overall algorithm implementation and execution complex
3. **Tedious Evaluation**: Algorithm evaluation requires real-time monitoring of distributed modules, and any changes require repeated evaluations

## 📦 Quick Start

### Requirements
- Java 11
- Maven 3.6+
- Docker
- Node.js 22+ (for frontend development)

### Installation

1. **Clone the project**
   ```bash
   git clone https://github.com/leiyu-dev/docker-TEQ.git
   cd docker-TEQ
   ```

2. **Compile backend project**
   ```bash
   mvn clean compile
   ```

3. **Start the testbed**
   ```bash
   ./run.sh
   ```

4. **Launch Web interface** (optional)
   ```bash
   cd front/visualizer
   npm install
   npm run dev
   ```
Web interface will be integrated into the java application in the near future

## 🚀 Algorithm Implementation Examples

### Simulation Example

```java
public class Example {
    public static void main(String[] args) {
        // 创建节点参数
        DockerNodeParameters param = new DockerNodeParameters();
        param.setCpuUsageRate(0.5);
        
        // 创建不同类型的节点
        EndDevice endDevice = new EndDevice();
        Coordinator coordinator = new Coordinator();
        Worker worker = new Worker();
        DataCenter dataCenter = new DataCenter();
        
        // 创建分层架构
        Layer endDeviceLayer = new Layer(endDevice, 300, "EndDeviceLayer");
        Layer coordinatorLayer = new Layer(coordinator, 1, "CoordinatorLayer");
        Layer workerLayer = new Layer(worker, 3, "WorkerLayer");
        Layer dataCenterLayer = new Layer(dataCenter, 1, "DataCenterLayer");
        
        // 启动仿真器
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

## 📊 Performance Monitoring Metrics

Teq provides comprehensive performance metric monitoring:

### Effectiveness Metrics
- **Query Accuracy**: Correctness verification of query results
- **Query Completeness**: Result coverage and completeness analysis

### Efficiency Metrics  
- **Processing Latency**: End-to-end processing time for individual queries
- **Transfer Latency**: Data transmission time between nodes
- **Throughput**: Number of queries/data items processed per second
- **Memory Usage**: Heap memory usage of each node
- **Energy Estimation**: Energy consumption calculation based on data processing volume

### System-wide Metric Transformation
```java
// Example: Overall query energy consumption calculation
double totalEnergyConsumption = 
    computingEnergy(nodes) + communicationEnergy(pipes);
    
// Where:
// computingEnergy = Σ(N_i × δ_i) for all nodes i
// communicationEnergy = Σ(N_p × κ_p) for all pipes p
```

## 🔧 Data Playback Control

### Data Stream Control
- **Controllable Flow Rate**: Set data generation speed through configurator
- **Order Guarantee**: Support fixed latency mode to ensure data order

### Query Stream Control  
- **Query File Format**: Query instances stored in query time order
- **Parameter Combinations**: Support random parameter combinations for continuous query streams
- **Frequency Control**: Configurable query generation frequency



## 📈 Comparison with Existing Tools

| Feature | EmuFog | iFogSim | EdgeCloudSim | IoTSim-Edge | PureEdgeSim | **Teq** |
|---------|---------|---------|--------------|-------------|-------------|----------|
| Network Model | ❌ | 🔸 | ✅ | ✅ | ✅ | ✅ |
| Device Characteristics | 🔸 | 🔸 | 🔸 | ✅ | ✅ | ✅ |
| Query Workload | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Real Tasks | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Metric Plots | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |

## 🤝 Academic Contributions

### Core Innovations
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
