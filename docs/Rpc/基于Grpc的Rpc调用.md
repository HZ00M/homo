
# 基于Grpc的Rpc调用

## 1. 概述

- **功能目标**: 
  - 通过持有grpc服务的facade接口，发起远程调用的能力
  - 提供负载均衡能力
  - 提供向指定pod发起远程调用能力


--- 

## 2. 环境要求

- **JDK 版本**: JDK 8 或更高版本。
- **依赖框架**: 基于homo-core的框架开发
- **构建工具**: Maven 3.6.0 或更高版本。
- **配置中心**：apollo
  ```text
    window环境：C:/opt/settings 配置server.properties文件 
    linux环境：opt/settings 配置server.properties文件 
  ```
  如下：
  ```properties
    apollo.meta=http://192.168.10.142:28080 
    env=PRO
    idc=dev-dubian
  ```
- **支持的操作系统**:
    - Windows 10 或更高
    - macOS 10.13 或更高
    - Linux (Ubuntu 18.04+)
---

## 3. 使用指南

### 3.1 添加 Maven 依赖

在项目的 `pom.xml` 文件中添加以下依赖：

#### rpc-server依赖:
```xml

<dependencies>
    <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-rpc-server</artifactId>
    </dependency>
</dependencies>
```
 
#### 基于grpc rpc
- 添加相关依赖依赖
```xml
<dependencies>
    <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-rpc-grpc</artifactId>
    </dependency>
</dependencies>
``` 
---
## 4. 快速入门
以下是一个简单的示例代码，展示如何使用如果定义一个基于http的rpc服务器：

- 创建一个homo-grpc-server-demo-facade,并添加相关依赖
```xml
<dependencies>
    <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-facade</artifactId>
    </dependency>
    <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-utils</artifactId>
    </dependency>
</dependencies>
```
- 声明一个rpc服务接口
```java
/**
 * 声明一个grpc无状态的主服务
 * host grpc-server-stateless
 * 端口 30302
 */
@ServiceExport(tagName = "grpc-server-stateless:30302",isMainServer = true,isStateful = false,driverType = RpcType.grpc)
@RpcHandler
public interface GrpcRpcServiceFacade {
  /**
   * 普通值调用
   * @param podId  pod id
   * @param parameterMsg  填充参数
   * @param param json 字符串
   */
  Homo<String> valueCall(Integer podId, ParameterMsg parameterMsg, String param);

  /**
   * pojo 对象参数调用
   * @param podId  pod id
   * @param parameterMsg  填充参数
   * @param paramVO  POJO
   */
  Homo<Integer> objCall(Integer podId, ParameterMsg parameterMsg, ParamVO paramVO);

  /**
   * protobuf 参数调用
   * @param podId  pod id
   * @param parameterMsg  填充参数
   * @param request protobuf 二进制
   */
  Homo<TestServerResponse> pbCall(Integer podId, ParameterMsg parameterMsg,TestServerRequest request);

  /**
   * 多值返回
   * @param podId
   * @param podId  pod id
   * @param parameterMsg  填充参数
   * @return  Tuple2<String,Integer>
   */
  Homo<Tuple2<String,Integer>> tuple2ReturnCall(Integer podId, ParameterMsg parameterMsg);
}
```
- 创建一个homo-grpc-server-demo模块，步骤如下：
  - 1：引入rpc模块并实现facade
      ```xml
    <dependencies>
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-server</artifactId>
        </dependency>
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-grpc-server-demo-facade</artifactId>
        </dependency>
        <!--        使用grpc-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-grpc</artifactId>
        </dependency>
    </dependencies>
      ```
    - 2：实现facade接口
      ```java
      @Component
      public class GrpcRpcService extends BaseService implements GrpcRpcServiceFacade {
        //省略实现细节
      }
      ```
    > **备注：**
    > 
    > 通过上述步骤，就实现了一个基于Grpc的rpc服务器了
    > 
    >     grpc service 
    >     
    >     基本函数签名为： Homo<Return> method(Integer podId,ParameterMsg parameterMsg,Param... params)
    > 
    >    Param参数类型：java基础类型，可序列化对象，proto对象
    > 
    >    Return返回类型：java基础类型，可序列化对象，proto对象
    > 
    >    Integer podId是填pod索引，用于对指定pod发起远程调用
    >    
    >    ParameterMsg parameterMsg 是框架填充参数，用于填充玩家信息，版本号等
    > 
    >      如下是合法签名：
    > 
    >     Homo<自定义Proto\> method(Integer podId, ParameterMsg parameterMsg, 自定义Proto req);
    > 
    >     Homo<String\> method(Integer podId, ParameterMsg parameterMsg, String param);
    > 
    >     Homo<Integer\> method(Integer podId, ParameterMsg parameterMsg, ParamVO paramVO);
    >
    >     Homo<Tuple2<String, Integer>> method(Integer podId, ParameterMsg parameterMsg); 
    >   
    > 
    > 

- 基于rpc客户端发起对rpc服务器的远程调用
  - 创建一个rpc客户端
    ```xml
        <dependencies>
        <!--        rpc客户端-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-client</artifactId>
        </dependency>
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-grpc</artifactId>
        </dependency> 
    
        <!--        homo-grpc-server服务facade-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-grpc-server-demo-facade</artifactId>
        </dependency>
        </dependencies>
     ```
    ```java
       @Component
       public class RpcHttpCallService {
          @Autowired(required = false)
          GrpcRpcServiceFacade rpcService;
          //注入后就可以像调用本地方法一样调用远程方法了
          public void call(){
              rpcService.method(req).start();
          }   
        }
    ```
    > **备注**
    > 
    > 本地运行时,需要在C:\Windows\System32\drivers\etc\hosts文件配置本地地址映射
    > 
    > 如： 127.0.0.1 grpc-server-stateless
    >

--- 

## 5. 定制化
- rpc客户端可选配置
```java
    public class RpcGrpcClientProperties {
    /**
     * 是否使用默认线程池执行
     */
    @Value("${homo.rpc.client.isDirector:true}")
    private boolean isDirector;
    /**
     * 检查目标服可用列表延迟时间
     */
    @Value("${homo.rpc.client.checkDelaySecond:0}")
    private int checkDelaySecond;
    /**
     * 检查目标服可用列表频率
     */
    @Value("${homo.rpc.client.checkPeriodSecond:5}")
    private int checkPeriodSecond;
    /**
     * netty客户端工作线程
     */
    @Value("${homo.rpc.client.channel.workerThread:2}")
    private int workerThread;
    /**
     * 消息最大长度，默认5mb
     */
    @Value("${homo.rpc.client.channel.messageMaxSize:5242880}")//5MB
    private int messageMaxSize;
    /**
     * 连接空闲保活时间
     */
    @Value("${homo.rpc.client.channel.channelKeepLiveMillsSecond:5000}")
    private int channelKeepLiveMillsSecond;
    /**
     * 客户端连接超时时间
     */
    @Value("${homo.rpc.client.channel.channelKeepLiveTimeoutMillsSecond:5000}")
    private int channelKeepLiveTimeoutMillsSecond;
}
```
- rpc客户端可选配置
```java
    public class RpcGrpcClientProperties {
    /**
     * 是否使用默认线程池执行
     */
    @Value("${homo.rpc.client.isDirector:true}")
    private boolean isDirector;
    /**
     * 检查目标服可用列表延迟时间
     */
    @Value("${homo.rpc.client.checkDelaySecond:0}")
    private int checkDelaySecond;
    /**
     * 检查目标服可用列表频率
     */
    @Value("${homo.rpc.client.checkPeriodSecond:5}")
    private int checkPeriodSecond;
    /**
     * netty客户端工作线程
     */
    @Value("${homo.rpc.client.channel.workerThread:2}")
    private int workerThread;
    /**
     * 消息最大长度，默认5mb
     */
    @Value("${homo.rpc.client.channel.messageMaxSize:5242880}")//5MB
    private int messageMaxSize;
    /**
     * 连接空闲保活时间
     */
    @Value("${homo.rpc.client.channel.channelKeepLiveMillsSecond:5000}")
    private int channelKeepLiveMillsSecond;
    /**
     * 客户端连接超时时间
     */
    @Value("${homo.rpc.client.channel.channelKeepLiveTimeoutMillsSecond:5000}")
    private int channelKeepLiveTimeoutMillsSecond;
}
```
- grpc服务端自定义配置
```java
    public class RpcGrpcServerProperties {
    /**
     * server核心线程数
     */
    @Value("${homo.rpc.server.grpc.thread.size:3}")
    private int corePoolSize;
    /**
     * 核心线程池保活时间
     */
    @Value("${homo.rpc.server.grpc.thread.keepLive:0}")
    private int keepLive;
    /**
     * boos线程数
     */
    @Value("${homo.rpc.server.grpc.boss.thread.size:1}")
    private int boosThreadSize;
    /**
     * 工作这线程数
     */
    @Value("${homo.rpc.server.grpc.worker.thread.size:2}")
    private int workerThreadSize;
    /**
     * 最大包限制，默认5MB
     */
    @Value("${homo.rpc.server.grpc.message.maxInboundMessageSize:5242880}")//5M
    private int maxInboundMessageSize;
    /**
     * 请求保活时间
     */
    @Value("${homo.rpc.grpc.server.message.permitKeepAliveTime:5000}")
    private int permitKeepAliveTime;
}
```
---
## 6. 演示案例
  [homo-grpc-server-demo](。./../../../homo-core-test/homo-grpc-server-demo)

<span style="font-size: 20px;">[返回主菜单](../../README.md)