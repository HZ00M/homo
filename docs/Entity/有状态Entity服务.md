
# 有状态Entity服务

## 1. 概述

- **功能目标**: 
  - 为玩家提供固定的节点访问能力
  - 提供玩家在服务中数据存储能力
  - 基于数据对象直接进行远程调用能力
  - 提供对象本地缓存的能力

## 2. 环境要求
 
- **依赖框架**: 基于homo-core的框架开发
- **数据库**: Mysql
```text
  用于数据持久化  
```
- **缓存**: Redis
```text
  提供用户entity数据存储, 节点状态存储，调用规则存储
```
- **云运行环境**: K8S 
- **本地运行环境**：windows、linux
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
---
## 3. 功能介绍
- **Entity**
  ```text
     Entity是基于homo框架的对数据进行抽象封装的领域对象，Entity对象提供自动落地功能、有状态的远程调用能力。
  
    ```
- **@StorageTime**  
  配置落地时间
  ```java 
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StorageTime {
    long value() default 0;
   }
  ```
- **@CacheTime**  
  配置二级缓存有效时间,二级缓存是存储在本地内存的缓存数据
  ```java
  @Retention(RetentionPolicy.RUNTIME)
  public @interface CacheTime {
  long value() default 0;
  }
  ```
---

## 4. 使用指南

### 4.1 添加 Maven 依赖

在项目的 `pom.xml` 文件中添加以下依赖：
 
#### entity能力
- 添加相关依赖依赖
  ```xml
        <!--        提供entity有状态服务-->
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-core-entity-ability</artifactId>
        </dependency>
  ```
---
## 5. 快速入门
以下是一个简单的示例代码，展示如何使用Entity开发业务逻辑：
- 创建一个homo-stateful-entity-demo-facade项目,并添加相关依赖
  ```xml
      <dependencies>
          <dependency>
              <groupId>com.homo</groupId>
              <artifactId>homo-core-entity-ability</artifactId>
          </dependency>
      </dependencies>
  ```

- 声明一个用户entity，以下声明了一个UserEntityFacade的entity的facade，并声明了每10秒落地1次，二级缓存有效期20秒
  ```java
  @EntityType(type = "userDocument-entity")
          @StorageTime(10000)
          @CacheTime(20000)
          public interface UserEntityFacade {
            Homo<QueryInfoResponse> queryInfo(QueryInfoRequest request);
      
            Homo<EnterGameResponse> enterGame(EnterGameRequest request);
      
            Homo<LeaveGameResponse> leaveGame(LeaveGameRequest request);
          }
  ```

- 创建一个homo-stateful-entity-demo实现上述facade的服务器逻辑，步骤如下：
  - 1：实现facade模块
      ```xml
    <dependencies>
        <dependency>
            <groupId>com.homo</groupId>
            <artifactId>homo-stateful-entity-demo-facade</artifactId>
        </dependency>
    </dependencies>
      ```
  - 2：实现facade接口，UserEntity继承BaseAbilityEntity并实现UserEntityFacade接口（BaseAbilityEntity提供Entity存储和调用的基础能力）
  ```java
    public class UserEntity extends BaseAbilityEntity implements UserEntityFacade {
        // lastQueryTime,playRecords这些是用户自定义的业务数据，这些数据由entity自动存储，无需用户操作数据库
        public long lastQueryTime; 
        public Map<String, PlayRecord> playRecords = new HashMap<>();
      //省略实现细节 
     }
  ``` 
  - 注册创建entity方法，当服务中不存在entity时，将调用该函数进行创建
  ```java
    @Autowired
    AbilityEntityMgr abilityEntityMgr;
    @Override
    public void afterServerInit() {
        abilityEntityMgr.registerEntityNotFoundProcess(UserEntity.class,((aClass, id) -> abilityEntityMgr.createEntityPromise(aClass,id)));
    }
  ```
  > **备注：**
  >
  > Entity采用fastjson序列化进行存储，因此需要落地的数据需要满足以下要求之一
  >
  > 1：由public修饰 2：定义set、get方法
  >
  > 通过上述步骤，就完成了一个entity服务的开发。
  >
 
- 创建一个homo-client-mock模拟客户端工程进行entity远程调用，通过entity的facade发起对服务器的远程调用
  - 添加userEntity的facade依赖
    ```xml
        <dependencies>
          <dependency>
              <groupId>com.homo</groupId>
              <artifactId>homo-stateful-entity-demo-facade</artifactId>
          </dependency>
        </dependencies>
     ```
  - 发起entity调用
    ```java
    @Slf4j
    @Component
    public class ClientEntityService extends BaseService implements ClientEntityServiceFacade {
  
        @Autowired
        EntityProxyFactory entityProxyFactory;
        @Autowired
        StorageEntityMgr entityMgr;  
  
        @Override
        public Homo<Long> queryRemoteServerInfo(Integer pod, ParameterMsg parameterMsg) {
            QueryInfoRequest request = QueryInfoRequest.newBuilder().setChannelId(parameterMsg.getChannelId()).build();
            UserEntityFacade serverEntity = entityProxyFactory.getEntityProxy(EntityServiceFacade.class, UserEntityFacade.class, parameterMsg.getUserId());
            return serverEntity.queryInfo(request)
                    .nextValue(ret -> {
                        long queryTime = ret.getBeforeQueryTime();
                        log.info("queryRemoteServerInfo queryTime {}", queryTime);
                        return queryTime;
                    });
        }
    } 
    ```
    
    >   **备注**
    >
    >   本地运行时,需要在C:\Windows\System32\drivers\etc\hosts文件配置本地地址映射
    >
    >   如： 127.0.0.1 client-entity-demo-0.client-entity-demo
    > 
    > 127.0.0.1 entity-server-demo-0.entity-server-demo
    >      
    > 

--- 

## 6. 定制化
- entity全局可选配置
```java
  public class AbilityProperties {
    /**
     * 定时更新频率 默认1秒
     */
    @Value("${homo.ability.storage.landing.interval.secondMillis:1000}")
    private long intervalSecondMillis;
    /**
     * 默认entity扫描路径
     */
    @Value("${homo.ability.storage.scan.entity.path:com.homo}")
    private String entityScanPath;
    /**
     * 默认缓存时间
     */
    @Value("${homo.ability.storage.default.cache.time.secondMillis:0}")
    private long cacheTimeSecondMillis;
    /**
     * 默认存储时间
     */
    @Value("${homo.ability.storage.default.save.time.secondMillis:60000}")
    private long saveTimeSecondMills;
  }
```
---
## 6. 演示案例
[homo-stateful-entity-demo](。./../../../homo-core-test/homo-stateful-entity-demo)