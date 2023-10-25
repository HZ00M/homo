# Rpc框架使用说明文档
## 简介
```text
    homo框架远程调用组件
```

## 目的
- 屏蔽远程调用细节，用户可根据需要选取不同的远程调用插件
- 支持有状态调用和无状态调用，能进行自负载均衡
- 支持高吞吐场景，支持高并发
## 前提
- 基于homo-core的存储框架
## 版本
- homo-core >= 1.0

## 接口设计
```java
/**
 * 暴露一个接口类为服务接口
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExport {
    String tagName() default ""; //服务名（格式：serviceName:port）
    RpcType driverType() default RpcType.grpc;    //服务类型
    boolean isStateful() default true; //是否是有状态服务器
    boolean isMainServer(); //是否是主服务，一个进程可能有多个服务，主服务用来向外部发起调用
}
/**
 * rpc类型  目前支持http和grpc
 */
public enum RpcType {
    http,grpc
}
```
## 使用说明
###例1：Http调用
- 1依赖工程
```text
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-server</artifactId>
        </dependency>  
        <!--        使用http-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-http</artifactId>
        </dependency>
```
- 2声明接口
```java
/**
 * 声明一个http无状态服务
 */
@ServiceExport(tagName = "http-server:30013",isMainServer = false,isStateful = false,driverType = RpcType.http)
@RpcHandler
public interface RpcHttpServiceFacade {

    Homo<JSONObject> jsonGetJson(JSONObject header);

    Homo<String> jsonGetStr(JSONObject header);

    Homo<String> jsonPost(JSONObject header, JSONObject req);

    Homo<String> jsonPostArray(JSONObject header, JSONArray jsonArray);

    Homo<String> postValue(JSONObject header,String value);

    Homo<TestServerResponse> pbPost(HttpHeadInfo header,TestServerRequest req);

}
```
- 3客户端工程依赖
```text
        <!--        目标服务器的接口-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-server-facade-test</artifactId>
        </dependency>
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-server</artifactId>
        </dependency>  
        <!--        使用http-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-http</artifactId>
        </dependency>
```
- 4发起远程调用

```java
@Component
public class RpcHttpCallTest {
    @Autowired(required = false)
    RpcHttpServiceFacade rpcService;


    public void testJsonGet() throws InterruptedException {
        JSONObject header = new JSONObject();
        header.put("token", "123");
        rpcService.jsonPost(JSON.parseObject(JSONObject.toJSONString(testObjParam)), header)
                .consumerValue(ret -> {
                    log.info("jsonPost ret {}", ret);
                }).start();
    }

    
    public Homo testJsonPost() throws InterruptedException {
        TestObjParam testObjParam = new TestObjParam();
        JSONObject header = new JSONObject();
        header.put("token", "123");
        return rpcService.jsonPost(JSON.parseObject(JSONObject.toJSONString(testObjParam)), header);
    }

    
    public void testPbPost() throws InterruptedException {
        HttpHeadInfo header = HttpHeadInfo.newBuilder().putHeaders("key", "value").build();
        TestServerRequest request = TestServerRequest.newBuilder().setParam("123").build();
        rpcService.pbPost(header, request)
                .consumerValue(ret -> {
                    log.info("objCall ret {}", ret);
                }).start();
    }
}
```

###例2：grpc调用
- 1依赖工程
```text
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-server</artifactId>
        </dependency>  
        <!--        使用grpc-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-grpc</artifactId>
        </dependency>
```
- 2声明接口
```java
/**
 * 声明一个grpc有状态服务
 */
@ServiceExport(tagName = "rpc-server:30011",isMainServer = true,isStateful = true,driverType = RpcType.grpc)
@RpcHandler
public interface RpcStatefulServiceFacade {
    Homo<JSONObject> jsonCall(Integer podId, ParameterMsg parameterMsg, JSONObject jsonStr);

    Homo<Integer> objCall(Integer podId, ParameterMsg parameterMsg,TestObjParam testObjParam);

    Homo<TestServerResponse> pbCall(Integer podId, ParameterMsg parameterMsg,TestServerRequest request);

    Homo<Tuple2<String,Integer>> tupleCall(Integer podId, ParameterMsg parameterMsg);
}
```
- 3客户端工程依赖
```text
        <!--        目标服务器的接口-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-server-facade-test</artifactId>
        </dependency>
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-server</artifactId>
        </dependency>  
        <!--        使用grpc-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-rpc-grpc</artifactId>
        </dependency>
```
- 4发起远程调用

```java
@Slf4j
@SpringBootTest(classes = TestRpcClientApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RpcStatefulCallTest {
    /**
     * 使用目标服务器接口发起远程调用
     */
    @Autowired(required = false)
    RpcStatefulServiceFacade rpcService;

    @Test
    public void testPbCall1()  {
        StepVerifier.create(
                rpcService.pbCall(0, ParameterMsg.newBuilder().setUserId("1_1").build(),TestServerRequest.newBuilder().setParam("123").build())
                        .nextValue(TestServerResponse::getCode)
        )
                .expectNext(123)
                .verifyComplete();

    }

    @Test
    public void testPbCall2()  {
        StepVerifier.create(
                rpcService.pbCall(-1, ParameterMsg.newBuilder().setUserId("1_1").build(),TestServerRequest.newBuilder().setParam("123").build())
                        .nextValue(TestServerResponse::getCode)
        )
                .expectNext(123)
                .verifyComplete();
    }

    @Test
    public void testObjCall()  {
        TestObjParam testObjParam = new TestObjParam();
        StepVerifier.create(
                rpcService.objCall(0, ParameterMsg.newBuilder().build(),testObjParam)
        )
                .expectNext(1)
                .verifyComplete();

    }

    @Test
    public void testJsonCall()  {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("1","1");
        StepVerifier.create(
                rpcService.jsonCall(0, ParameterMsg.newBuilder().build(),jsonObject)
                        .nextValue(ret-> ret.get("2"))
        )
                .expectNext("2")
                .verifyComplete();
    }
}
```
###定制化
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
- 有状态rpc配置
```java
public class ServerStateProperties {
    /**
     * 本地内存用户连接信息保持时间(10分钟)
     */
    @Value("${homo.service.state.local.cache.duration.second:600}")
    private int localUserServicePodCacheSecond;
    /**
     * 服务器缓存用户连接信息保持时间(比连接短1分钟)
     */
    @Value("${homo.service.state.cache.duration.second:540}")
    private int remoteUserServicePodCacheSecond;

    /**
     * 服务器缓存用户连接信息延迟删除时间，默认60秒
     */
    @Value("${homo.service.state.cache.delay.remove.second:60}")
    private int remoteUserServicePodDelayRemoveSecond;

    /**
     * 服务器状态超时时间，默认60秒
     */
    @Value("${homo.service.state.expire.seconds:60}")
    private long serviceStateExpireSeconds;

    /**
     * 服务器状态更新间隔
     */
    @Value("${homo.service.state.update.seconds:30}")
    private int serviceStateUpdatePeriodSeconds;

    /**
     * 负载均衡因子 （0~1）0.5表示负载均衡0.5参考cpu，0.5参考内存值
     */
    @Value("${homo.service.state.cpu.factor:0.5}")
    private float cpuFactor;

    /**
     * 服务良好状态配置
     * Map<serviceName,goodState>
     */
    @Value("#{${homo.service.state.range:{}}}")
    private Map<String,Integer> goodStateRange = new HashMap<>();

    /**
     * 服务负载基数，提供一个缓冲值
     * Map<serviceName,goodState>
     */
    @Value("${homo.service.state.range.default:500}")
    private int defaultRange;   
}
```