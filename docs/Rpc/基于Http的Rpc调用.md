
# 基于Http的Rpc调用

## 1. 概述 
- **文档目标**: 
  - 通过持有http服务的facade接口，发起远程调用的能力
  - 提供负载均衡能力
  - 提供无状态的，自动负载均衡的远程调用能力

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

## 3. 安装指南

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

#### 基于http rpc
- 添加相关依赖依赖
```xml
<dependencies>
    <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-rpc-http</artifactId>
    </dependency>
</dependencies>
```  
---
## 4. 快速入门
以下是一个简单的示例代码，展示如何使用如果定义一个基于http的rpc服务器：
- 创建一个homo-http-server-facade-demo,并添加相关依赖
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
 * 声明一个http无状态的主服务
 * host http-rpc-demo-server
 * 端口 30300
 */
@ServiceExport(tagName = "http-rpc-demo-server:30300",isMainServer = true,isStateful = false,driverType = RpcType.http)
@RpcHandler
public interface HttpRpcDemoServiceFacade {

  /**
   * get 请求
   * @param header 请求头
   * @return 返回 JSONObject
   */
  Homo<JSONObject> jsonGetJson(JSONObject header);

  /**
   * post 请求
   * @param header 请求头
   * @param req 请求体
   * @return 返回 String
   */
  Homo<JSONObject> jsonPost(JSONObject header, JSONObject req);

  /**
   * post 请求
   * @param header 请求头
   * @param jsonArray 请求体
   * @return 返回 String
   */
  Homo<String> jsonPostArray(JSONObject header, JSONArray jsonArray);

  /**
   * post 请求
   * @param header 请求头
   * @param value 请求值
   * @return 返回 String
   */
  Homo<String> valuePost(JSONObject header, String value);

  /**
   * post 请求 (pb协议)
   * @param headerInfo 通用的pb请求头
   * @param req 自定义pb请求体
   * @return
   */
  Homo<HttpServerResponsePb> pbPost(HttpHeadInfo headerInfo, HttpServerRequestPb req);

}
```
- 创建一个homo-http-server-demo模块，步骤如下：
  - 1：引入rpc模块并及facade
      ```xml
       <dependencies>
         <!--        开启rpc服务-->
         <dependency>
             <groupId>com.homo</groupId>
             <artifactId>homo-core-rpc-server</artifactId>
         </dependency>
         <!--        使用http-->
         <dependency>
             <groupId>com.homo</groupId>
             <artifactId>homo-core-rpc-http</artifactId>
         </dependency>
         <!--        实现facade接口-->
         <dependency>
             <groupId>com.homo</groupId>
             <artifactId>homo-http-server-demo-facade</artifactId>
         </dependency>
     </dependencies>
      ```
  - 2：实现facade接口
    ```java
    @Component
    public class RpcHttpService extends BaseService implements HttpRpcDemoServiceFacade {
      //省略实现细节
    }
    ```
    > **备注：**
    > 
    > 通过上述步骤，就实现了一个基于Http的rpc服务器了
    > 
    >   基于http的rpc请求支持以下签名接口
    > 
    >   Homo<JSONObject\> method(JSONObject header);
    > 
    >   Homo<JSONObject\> method(JSONObject header, JSONObject req);
    >
    >   Homo<JSONObject\> method(JSONObject header, JSONArray jsonArray);
    > 
    >   Homo<String\> method(JSONObject header, String value);
    > 
    >   Homo<自定义Proto\> method(HttpHeadInfo headerInfo, 自定义Proto req);
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
            <artifactId>homo-core-rpc-http</artifactId>
        </dependency>
    
        <!--        rpc服务facade-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-http-server-demo-facade</artifactId>
        </dependency> 
        </dependencies>
     ```
    ```java
       @Component
       public class RpcHttpCallService {
          @Autowired(required = false)
          HttpRpcDemoServiceFacade rpcDemoService; 
          //注入后就可以像调用本地方法一样调用远程方法了
          public void call(){
              rpcDemoService.method(req).start();
          }   
        }
    ```
    > **备注**
    > 
    > 本地运行时,需要在C:\Windows\System32\drivers\etc\hosts文件配置本地地址映射
    > 
    > 如： 127.0.0.1 http-rpc-demo-server
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
- http-rpc服务端自定义配置
```java
 public class RpcHttpServerProperties {
  /**
   * http 最大消息大小
   */
  @Value("${homo.rpc.server.http.bytesLimit:614400}")//600 * 1024
  private int bytesLimit;
}
```