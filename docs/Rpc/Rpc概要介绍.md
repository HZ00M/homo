
# Rpc框架使用说明文档

## 1. 概述

- **homo框架远程调用组件**: 
- **版本**: [版本号，homo-core >= 1.0]
- **功能简介**:
    - 提供用于服务间的内部调用，包括 [http通信、grpc通讯、负载均衡、有状态调用、无状态调用]。
    - 支持 [2D 游戏、3D 游戏、多玩家实时对战等] 场景。
- **文档目标**: 
  - 屏蔽远程调用细节，用户可根据需要选取不同的远程调用插件
  - 支持有状态调用和无状态调用，能进行自负载均衡
  - 支持高吞吐场景，支持高并发

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

## 3. Rpc指南 
---
#### 核心功能模块
- @ServiceExport
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
    boolean isMainServer(); //一个进程可能有多个服务，但只能有一个主服务，用来向外部进行统一调用及对外暴露统一host
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
<span style="font-size: 20px;">[返回主菜单](../../README.md)