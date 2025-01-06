# Netty RPC 示例工程

本仓库包含了一组基于 Netty 的 RPC 示例，展示了远程通信的不同复杂度和功能特性。工程分为三个模块：

## 工程结构
```
├── .idea
├── src
│   ├── main
│   │   ├── java
│   │   │   └── org
│   │   │       ├── infraRpcExample
│   │   │       ├── remoteRpcExample
│   │   │       └── simpleRpcExample
│   │   └── resources
│   └── test
│       └── java
│           └── org
│               └── infraRpcExample
└── target
```

## 模块介绍

### 1. **simpleRpcExample**
一个基础的 Netty 通信示例，适合初学者了解 RPC 通信的基本概念。

#### 功能特点：
- 基本的客户端-服务器通信。
- 简单的消息交换。

### 2. **remoteRpcExample**
一个中级示例，展示了稍微复杂的远程通信场景。该模块基于 simpleRpcExample 模块，增加了更多功能。

#### 功能特点：
- 支持远程方法调用。
- 更复杂的协议设计。

### 3. **infraRpcExample**
一个面向生产环境的高级示例，展示了实现基于 Netty 的远程通信的最佳实践和高级功能。该模块适用于实际应用场景。

#### 功能特点：
- 支持多种序列化/反序列化方式（如 Kryo、ProtoStuff）。
- 多线程请求处理。
- 支持流式通信。
- 高级异常处理和重试机制。
- 动态主机管理，支持 Eureka 和 Zookeeper。
- 模块化架构，便于扩展和维护。

#### 子包介绍：
- **app**：应用逻辑。
- **codec**：序列化与反序列化逻辑。
- **common**：通用工具和常量。
- **config**：配置类。
- **controller**：处理请求的控制器。
- **dao**：数据访问对象和数据库交互。
- **enums**：枚举定义。
- **exceptions**：自定义异常处理。
- **future**：异步请求处理。
- **handler**：请求与响应处理器。
- **host**：主机管理与选择逻辑。
- **listener**：事件监听器。
- **protocol**：RPC 协议定义。
- **registry**：服务注册与发现。
- **remote**：远程调用逻辑。
- **request/response**：请求与响应模型。
- **service**：服务层实现。
- **shell**：命令行交互工具。
- **thread**：线程管理工具。
- **utils**：通用工具类。

## 快速开始

### 前置条件
- Java 8 或更高版本
- Maven

### 构建与运行
1. 克隆仓库：
   ```bash
   git clone https://github.com/sekingme/PowerNetty.git
   cd PowerNetty
   ```
2. 构建项目：
   ```bash
   mvn clean install
   ```
3. 运行各模块：
    - 运行 `simpleRpcExample`：
      ```bash
      java -cp target/simpleRpcExample.jar org.simpleRpcExample.Main
      ```
    - 运行 `remoteRpcExample`：
      ```bash
      java -cp target/remoteRpcExample.jar org.remoteRpcExample.Main
      ```
    - 运行 `infraRpcExample`：
      ```bash
      java -cp target/infraRpcExample.jar org.infraRpcExample.Main
      ```

## 贡献
欢迎贡献代码！如有建议或改进，请提交 Pull Request 或创建 Issue。

## 许可证
本项目使用 MIT 许可证，详见 `LICENSE` 文件。

## 联系方式
如有问题或建议，请联系 [email:sekingme@163.com]。

