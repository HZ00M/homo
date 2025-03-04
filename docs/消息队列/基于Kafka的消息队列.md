
# 基于Kafka的消息队列

## 1. 概述

- **功能目标**: 
  - kafka的mq消息队列实现

---
## 2. 环境要求

- **JDK 版本**: JDK 8 或更高版本。
- **依赖框架**: 基于homo-core的框架开发
- **构建工具**: Maven 3.6.0 或更高版本。
- **消息队列**: 启动apache kafka服务。
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
- **创建用于配置kafka的namspace**：
 
  application.properties配置如下，其中namespace是homo_mq_kafka的配置kafka相关信息，用户也可创建自己自定义的namespace
  ```text
  apollo.bootstrap.enabled = true
  apollo.bootstrap.namespaces = application,homo_redis_config,redis_connect_info,homo_root_info,homo_zipkin_config,homo_mq_kafka
  spring.main.web-application-type=none
  ```
homo_mq_kafka参考配置
```text
homo.mq.kafka.bootstrap.servers = kafka-service-client:9093
homo.mq.kafka.consumer.auto.commit = true

```
- **支持的操作系统**:
    - Windows 10 或更高
    - macOS 10.13 或更高
    - Linux (Ubuntu 18.04+)
---

## 3. 使用指南

### 3.1 添加依赖
在项目的 `pom.xml` 文件中添加以下依赖：
-  开启生产者：homo-core-mq-producer
-  生产者kafka驱动：homo-core-mq-driver-kafka
```xml

<dependencies>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-mq-producer</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-mq-driver-kafka</artifactId>
  </dependency>
</dependencies>
```

-  开启消费者：homo-core-mq-consumer
-  生产者kafka驱动：homo-core-mq-driver-kafka
```xml

<dependencies>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-mq-consumer</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-mq-driver-kafka</artifactId>
  </dependency>
</dependencies>
```

## 4. 快速入门
以下是一个简单的示例代码，展示如何使用如何开启生产者服务并发送message：
- 创建一个homo-core-mq-producer-demo,并添加相关依赖
```xml
<dependencies>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-mq-producer</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-mq-driver-kafka</artifactId>
  </dependency>
</dependencies>
```
- 创建MQProducer
```java
@Service
@Slf4j
public class MQProducerTestService implements ServiceModule {
  @Autowired
  @Lazy  //需要将依赖加入lazy，否则这个bean会提前创建  会影响HomoLogHandler的最优先创建
  MQProducerFactory mqProducerFactory;
  MQProducer mqProducer;
  @Override
  public void afterAllModuleInit() {
    MQProducerConfig config = new MQProducerConfig(MQType.KAFKA, appId, regionId, TopicResolveStrategyEnum.APPEND_APP_ID_SERVER_ID);
    mqProducer = mqProducerFactory.create(config);
  }
}
```
- 发送消息
```java
@Service
@Slf4j
public class MQProducerTestService implements ServiceModule {  
    public void fun() {
      jsonObject = new JSONObject();
      jsonObject.put("topic", TopicConstant.topic_1);
      jsonObject.put("sendCount", sendCount);
      mqProducer.send(TopicConstant.topic_1, "key", jsonObject, new ProducerCallback() {
          @Override
          public void onCompletion(boolean ok, Throwable throwable) {
              log.info("onCompletion ret {}", ok, throwable);
          }
      });
    }
}
```
以下是一个简单的示例代码，展示如何使用如何消费生成者发送的message：
  - 创建一个homo-mq-consumer-demo模块，,并添加相关依赖
  引入rpc模块并实现facade
  ```xml
      <dependencies>
      <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-mq-consumer</artifactId>
      </dependency>
      <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-mq-driver-kafka</artifactId>
      </dependency>
    </dependencies>
   ```
  - 创建MqConsumer
  ```java
  @Service
  @Slf4j
  public class MQConsumerHandler2 implements SinkHandler,ServiceModule {
      @Autowired
      RootModule rootModule;
      @Autowired
      MQConsumerFactory mqConsumerFactory;
      MQConsumer mqConsumer;
      @Override
      public void moduleInit() {
        String appId = rootModule.getServerInfo().getAppId();
        String regionId = rootModule.getServerInfo().getRegionId();
        MQConsumerConfig config = new MQConsumerConfig(MQType.KAFKA, appId, regionId, "group-2", TopicResolveStrategyEnum.APPEND_APP_ID_SERVER_ID);
        mqConsumer = mqConsumerFactory.create(config);
      }
  }
  ```
  - 通过@SinkFunc方式消费
  ```java
  @Service
  @Slf4j
  public class MQConsumerHandler implements SinkHandler,ServiceModule {
      @Autowired
      RootModule rootModule;
      @Autowired
      MQConsumerFactory mqConsumerFactory;
      MQConsumer mqConsumer;
      @Override
      public void afterAllModuleInit() {
        mqConsumer.addReceiver(this);
        mqConsumer.start();
      }
      @SinkFunc(topics = {TopicConstant.topic_1,"111"})
      public void sinkFun1(String topic, JSONObject message, ConsumerCallback callback) {
        log.info("MQConsumerHandler1 sinkFun1 topic {} message {} ", topic, message);
        callback.confirm();
      }
    
      @SinkFunc(topics = {TopicConstant.topic_1})
      public void sinkFun2(String topic, JSONObject message, ConsumerCallback callback) {
        log.info("MQConsumerHandler1 sinkFun2 topic {} message {} ", topic, message);
        callback.confirm();
      }
  }
  ```
- 通过ReceiverSink函数消费
  ```java
  @Service
  @Slf4j
  public class MQConsumerHandler2 implements ServiceModule {
      @Autowired
      RootModule rootModule;
      @Autowired
      MQConsumerFactory mqConsumerFactory;
      MQConsumer mqConsumer;
  
      @Override
      public void moduleInit() {
          String appId = rootModule.getServerInfo().getAppId();
          String regionId = rootModule.getServerInfo().getRegionId();
          MQConsumerConfig config = new MQConsumerConfig(MQType.KAFKA, appId, regionId, "group-2", TopicResolveStrategyEnum.APPEND_APP_ID_SERVER_ID);
          mqConsumer = mqConsumerFactory.create(config);
      }
  
      @Override
      public void afterAllModuleInit() {
          try {
              ReceiverSink<JSONObject> receiverSink = new ReceiverSink<JSONObject>() {
                  @Override
                  public void onSink(String topic, JSONObject message, ConsumerCallback callback) {
                      log.info("MQConsumerHandler2 receiverSink topic {} message {} ", topic, message);
                      callback.confirm();
                  }
              };
              mqConsumer.addReceiver(TopicConstant.topic_1, receiverSink);
              mqConsumer.start();
          } catch (Exception e) {
              log.error("MQConsumerHandler2 afterAllModuleInit error", e);
          }
      }
  }
  ``` 

--- 

## 5. 定制化
- kafka 消息驱动可选配置
  ```java
  public class MQKafkaProperties {
  
    // Kafka 服务器地址列表，多个地址用逗号分隔，默认 localhost:9092
    @Value("${homo.mq.kafka.bootstrap.servers:localhost:9092}")
    private String servers;
  
    // Kafka 确认模式："all" 表示所有副本都确认接收，确保消息持久化
    @Value("${homo.mq.kafka.acks:all}")
    private String acks;
  
    // Kafka 生产者重试次数，默认 3 次
    @Value("${homo.mq.kafka.retries:3}")
    private Integer retries;
  
    // Kafka 生产者重试的时间间隔（毫秒），避免频繁重试导致服务器过载
    @Value("${homo.mq.kafka.retry.backoff.ms:100}")
    private Integer retryBackoffMs;
  
    // 生产者交付消息的超时时间（毫秒），包括所有重试时间，默认 120000ms (2分钟)
    @Value("${homo.mq.kafka.delivery.timeout.ms:120000}")
    private Integer deliveryTimeoutMs;
  
    // Kafka 缓冲区内存大小，用于缓存未发送的消息（字节），默认 32 MB
    @Value("${homo.mq.kafka.buffer.memory:33554432}")
    private Long bufferMemory;
  
    // 批量发送的消息总字节大小（字节），此处默认 16 KB
    @Value("${homo.mq.kafka.batch.size:16384}")
    private Integer batchSize;
  
    // 批量发送的等待时间（毫秒），在未达到批量大小时等待该时间后发送消息，默认 100 ms
    @Value("${homo.mq.kafka.linger.ms:100}")
    private Integer lingerMs;
  
    // Kafka 生产者队列容量，控制待发送消息的队列长度，默认 50000 条
    @Value("${homo.mq.kafka.producer.queue.capacity:50000}")
    private Integer producerQueueCapacity;
  
    // 生产者线程池核心线程数，默认 16
    @Value("${homo.mq.kafka.producer.pool.core.size:16}")
    private Integer producerPoolCoreSize;
  
    // 生产者线程池最大线程数，默认 16
    @Value("${homo.mq.kafka.producer.pool.max.size:16}")
    private Integer producerPoolMaxSize;
  
    // 生产者线程池空闲线程存活时间（秒），默认 60 秒
    @Value("${homo.mq.kafka.producer.pool.keepLive.second:60}")
    private Integer producerPoolKeepLive;
  
    // Kafka 消费者每次轮询最大拉取记录数，默认 60 条
    @Value("${homo.mq.kafka.consumer.max.poll.records.size:60}")
    private Integer maxPollRecords;
  
    // Kafka 消费者是否启用自动提交偏移量，默认 false（需要手动提交）
    @Value("${homo.mq.kafka.consumer.auto.commit:false}")
    private Boolean autoCommit;
  
    // Kafka 消费者拉取消息的最大等待时间（毫秒），默认 2000 ms
    @Value("${homo.mq.kafka.consumer.poll.wait.millisecond:2000}")
    private long pollWailMs;
  
    // Kafka 消费者使用的 Key 反序列化器类，默认 StringDeserializer
    @Value("${homo.mq.kafka.key.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private String keyDeserializer;
  
    // Kafka 消费者使用的 Value 反序列化器类，默认 BytesDeserializer
    @Value("${homo.mq.kafka.value.deserializer:org.apache.kafka.common.serialization.BytesDeserializer}")
    private String valueDeserializer;
  }
  ``` 
 ---
## 6. 演示案例
[homo-mq-consumer-demo](。./../../../homo-core-test/homo-mq-consumer-demo)
[homo-mq-producer-demo](。./../../../homo-core-test/homo-mq-producer-demo) 

<span style="font-size: 20px;">[返回主菜单](../../README.md)