
# 基于Mysql及Redis的存储能力实现

## 1. 概述

- **功能目标**: 
  - 提供统一的存储对象的能力
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
- **创建用于配置的namspace**：
 
application.properties配置如下，其中namespace是homo_redis_config,redis_connect_info,mysql-connect-info的配置storage的相关信息，
用户也可创建自己自定义的namespace
```text
  apollo.bootstrap.enabled = true
  apollo.bootstrap.namespaces =  application,homo_redis_config,redis_connect_info,mysql-connect-info
  spring.main.web-application-type=none
```
homo_redis_config参考配置
```text
homo.redis.timeOutMs = 300000
homo.redis.dataBase = 0
homo.redis.maxTotal = 100
homo.redis.maxIdle = 10
homo.redis.minIdel = 10
homo.redis.maxWaitMillis = -1
homo.redis.testOnBorrow = false
homo.redis.zkSessionTimeoutMs = 30000
homo.redis.soTimeOut = 30000
homo.redis.maxAttemps = 5
```
redis_connect_info参考配置
```text
homo.redis.url = homo-redis
homo.redis.port = 30379

```
mysql-connect-info参考配置
```text
homo.datasource.url = jdbc:mysql://homo-mysql:30006/homo
homo.datasource.host = 10.100.1.236
homo.datasource.username = root
homo.datasource.password = password

```
- **支持的操作系统**:
    - Windows 10 或更高
    - macOS 10.13 或更高
    - Linux (Ubuntu 18.04+)
---

## 3. 使用指南

### 3.1 添加依赖
在项目的 `pom.xml` 文件中添加以下依赖：
-  使用Storage Ddk：homo-core-storage 
```xml 
<dependency>
  <groupId>com.homo</groupId>
  <artifactId>homo-core-storage-redis-mysql</artifactId>
</dependency>
```


## 4. 快速入门
@DirtyLandingServer开启落地程序，可同时部署多个Pod开启并行落地能力，落地程序会定时将脏数据存储至mysql： 
- 创建一个springBoot服务，使用@DirtyLandingServer开启落地服务，如下
```java
@Slf4j
@SpringBootApplication
@DirtyLandingServer
public class PersistentTessApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(PersistentTessApplication.class);
  }

  @Override
  public void run(String... args) {}
}
```

## 5. 定制化
- 落地程序可选配置

**mysql相关配置:系统默认读取以下命名空间的mysql配置** 
```java
public class MysqlNamespaceProperties { 
    @Value("${mysql.public.namespace:homo_mysql_config}")
    private String publicNamespace;
    @Value("${mysql.private.namespace:mysql-connect-info}")
    private String privateNamespace;
}
```
**当不使用上述命名空间的mysql时。系统读取用户自定义的配置**
```java
/**
 * 配置类：MysqlProperties
 * <p>
 * 该类用于加载 MySQL 数据源的相关配置，支持 Spring 配置文件 (application.properties 或 application.yml) 注入。
 * 通过 @Value 注解获取外部配置，并提供默认值。
 * </p>
 */
@Configurable
@Data
public class MysqlProperties {

  /**
   * 数据库连接 URL，示例：
   * jdbc:mysql://localhost:3306/db_name
   */
  @Value("${homo.datasource.url:}")
  public String dbUrl;

  /**
   * 数据库主机地址
   */
  @Value("${homo.datasource.host:}")
  public String dbHost;

  /**
   * 数据库连接附加参数，例如时区、编码等
   */
  @Value("${homo.datasource.param:}")
  public String dbParam;

  /**
   * 数据库用户名
   */
  @Value("${homo.datasource.username:}")
  public String username;

  /**
   * 数据库密码
   */
  @Value("${homo.datasource.password:}")
  public String password;

  /**
   * MySQL JDBC 驱动类名，默认使用 MySQL 8.0+ 的驱动
   */
  @Value("${homo.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
  public String driverClassName; // 变量名 driverClassNam 可能有误，应为 driverClassName

  /**
   * 连接池初始化时的连接数，默认 5
   */
  @Value("${homo.datasource.initialSize:5}")
  public int initialSize;

  /**
   * 连接池最小空闲连接数，默认 5
   */
  @Value("${homo.datasource.minIdle:5}")
  public int minIdle;

  /**
   * 连接池最大活跃连接数，默认 20
   */
  @Value("${homo.datasource.maxActive:20}")
  public int maxActive;

  /**
   * 连接获取等待超时时间（毫秒），默认 60000ms（60秒）
   */
  @Value("${homo.datasource.maxWait:60000}")
  public int maxWait;

  /**
   * 连接池清理空闲连接的时间间隔（毫秒），默认 300000ms（5分钟）
   */
  @Value("${homo.datasource.timeBetweenEvictionRunsMillis:300000}")
  public int timeBetweenEvictionRunsMillis;

  /**
   * 连接最小空闲时间（毫秒），超时后会被回收，默认 60000ms（1分钟）
   */
  @Value("${homo.datasource.minEvictableIdleTimeMillis:60000}")
  public int minEvictableIdleTimeMillis;

  /**
   * 连接校验 SQL 语句，确保连接可用，默认 "SELECT 1 FROM DUAL"
   */
  @Value("${homo.datasource.validationQuery:SELECT 1 FROM DUAL}")
  public String validationQuery;

  /**
   * 是否在连接空闲时进行检查，默认 true
   */
  @Value("${homo.datasource.testWhileIdle:true}")
  public Boolean testWhileIdle;

  /**
   * 是否在获取连接时进行检查，默认 false
   */
  @Value("${homo.datasource.testOnBorrow:false}")
  public Boolean testOnBorrow;

  /**
   * 是否在归还连接时进行检查，默认 false
   */
  @Value("${homo.datasource.testOnReturn:false}")
  public Boolean testOnReturn;

  /**
   * 是否开启 PreparedStatement 预编译缓存，默认 true
   */
  @Value("${homo.datasource.poolPreparedStatements:true}")
  public Boolean poolPreparedStatements;

  /**
   * 连接池的 SQL 过滤器配置，默认 "stat,wall,log4j2"
   * - stat：SQL 监控统计
   * - wall：SQL 防火墙
   * - log4j2：日志记录
   */
  @Value("${homo.datasource.filters:stat,wall,log4j2}")
  public String filters;
}
``` 
**redis相关配置:系统默认读取以下命名空间的redis配置**
```java
public class RedisNamespaceProperties {
  @Value("${redis.public.namespace:homo_redis_config}")
  private String publicNamespace;
  @Value("${redis.private.namespace:redis_connect_info}")
  private String privateNamespace;
  @Value("${redis.type:LETTUCE_POOL}")
  private String redisType;
}
```
**当不使用上述命名空间的redis时。系统读取用户自定义的配置**
```java
/**
 * Redis 配置属性类
 * <p>
 * 该类用于读取 Redis 相关的配置信息，并通过 Spring 的 @Value 注解进行属性注入。
 * </p>
 */
@Configurable
@Data
public class RedisProperties {

  /**
   * Redis 服务器 URL（默认空）
   */
  @Value("${homo.redis.url:}")
  public String url;

  /**
   * Redis 端口号（默认 6379）
   */
  @Value("${homo.redis.port:6379}")
  public String port;

  /**
   * Redis 认证密码（默认空）
   */
  @Value("${homo.redis.auth:}")
  public String auth;

  /**
   * Redis 代理（默认空）
   */
  @Value("${homo.redis.proxyDir:}")
  public String proxyDir;

  /**
   * Redis 数据库索引（默认 0）
   */
  @Value("${homo.redis.dataBase:0}")
  public Integer dataBase;

  /**
   * Redis 连接超时时间（单位：毫秒，默认 300000）
   */
  @Value("${homo.redis.timeOutMs:300000}")
  public Integer timeOutMs;

  /**
   * 连接池最大连接数（默认 100）
   */
  @Value("${homo.redis.maxTotal:100}")
  public Integer maxTotal;

  /**
   * 连接池最大空闲连接数（默认 10）
   */
  @Value("${homo.redis.maxIdle:10}")
  public Integer maxIdle;

  /**
   * 连接池最小空闲连接数（默认 10）
   */
  @Value("${homo.redis.minIdle:10}")
  public Integer minIdle;

  /**
   * 连接池最大等待时间（单位：毫秒，默认 -1，表示无限等待）
   */
  @Value("${homo.redis.maxWaitMillis:-1}")
  public Integer maxWaitMillis;

  /**
   * 是否在借用连接时进行验证（默认 false）
   */
  @Value("${homo.redis.testOnBorrow:false}")
  public Boolean testOnBorrow;

  /**
   * Redis Socket 超时时间（单位：毫秒，默认 30000）
   */
  @Value("${homo.redis.soTimeOut:30000}")
  public Integer soTimeOut;

  /**
   * 最大重试次数（默认 5）
   */
  @Value("${homo.redis.maxAttemps:5}")
  public Integer maxAttemps;

  /**
   * Key 过期时间（单位：秒，默认 86400，即 24 小时）
   */
  @Value("${homo.redis.expire:86400}")
  public Integer expireTime;
}
``` 
<span style="font-size: 20px;">[返回主菜单](../../README.md)