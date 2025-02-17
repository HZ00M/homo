
# Document-Mongo驱动实现

## 1. 概述

- **功能目标**: 
  - 基于Mongo的响应式Document驱动实现
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
 
application.properties配置如下，其中namespace是mongo_connect_info的配置mongo的相关信息，用户也可创建自己自定义的namespace
```text
apollo.bootstrap.enabled = true
apollo.bootstrap.namespaces = application,mongo_connect_info
spring.main.web-application-type=none
```
homo_relational_mysql参考配置
```text
homo.mongo.enable = true
homo.mongo.connString = mongodb://10.100.2.170:37017
homo.mongo.database = homo_storage

```
- **支持的操作系统**:
    - Windows 10 或更高
    - macOS 10.13 或更高
    - Linux (Ubuntu 18.04+)
---

## 3. 使用指南

### 3.1 添加依赖
在项目的 `pom.xml` 文件中添加以下依赖：
-  使用封装的storege操作底层driver：homo-core-storage
-  使用mongo驱动：homo-core-document-mongo
```xml 
    <dependencies>
      <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-storage</artifactId>
      </dependency>
      <dependency>
        <groupId>com.homo</groupId>
        <artifactId>homo-core-document-mongo</artifactId>
      </dependency>
  </dependencies>
```
 

## 4. 快速入门
以下是一个简单的示例代码，展示如何使用如何使用sdk操作数据：
- 创建一个homo-document-mongo-demo,并添加相关依赖
```xml
    <dependencies>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-document-mongo-demo-facade</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-rpc-server</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-storage</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-document-mongo</artifactId>
  </dependency>
</dependencies>
```
- 创建一个文档定义类UserDocument，使用@Document声明文档名及创建相关索引，@HomoIndex定义索引信息，如下
```java
@Slf4j
@BsonDiscriminator
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "user", indexes = {"value.nickName", "value.bindGameList.gameName", "value.level", "value.registerTime", "value.tagList"})
public class UserDocument implements Serializable {
    private String userId;
    private String phone;
    private String nickName;
    private Integer age;
    private Integer sex;                                                //性别  0男 1女
    private String sign;
    private String birthday;
    private String avatar;
    private String identityId;                                          //身份证
    private String identityIdName;                                      //身份证姓名
    private Integer level;                                              //会员等级
    private Double totalCharge;                                         //总充值金额
    private Integer groupValue;                                         //成长值
    private String imei;                                                //imei
    private String platform;                                            //系统类型 ios or android
    private String deviceType;                                          //手机型号
    private String token;                                               //登陆token

    private Long registerTime;                                          //注册时间
    private Long inMemberTime;                                          //成为会员时间
    private Long lastLoginTime;                                         //最后一次登陆时间
    private Long lastChargeTime;                                        //最后一次充值时间

}
```
对应的数据库结构如下:其中value里的值即是UserDocument数据,其余为框架封装字段
```txt
{
    "_id" : ObjectId("67b2d1de7d16fae7400d5469"),
    "primaryKey" : "primary_UserLogicType_user_123_data",
    "isDel" : 0,
    "key" : "data",
    "queryAllKey" : "query_UserLogicType_user_123",
    "value" : {
        "userId" : "user_123",
        "nickName" : "guest",
        "age" : 100,
        "sex" : 0,
        "totalCharge" : 0.0,
        "groupValue" : 0,
        "registerTime" : NumberLong(1739772376952),
        "inMemberTime" : NumberLong(1739772376952),
        "lastLoginTime" : NumberLong(1739772376952),
        "lastChargeTime" : NumberLong(1739772376952)
    }
}

``` 
以下是一个简单的示例代码，展示如何使用Mongo驱动进行数据操作：
  - 业务bean中注入RelationalTemplate
```java
@Component
@Slf4j
public class MongoDocumentService extends BaseService implements MongoDocumentServiceFacade {
  /**
   * 使用响应式Mongo文档存储组件
   */
  @Autowired
  DocumentStorage<Bson, Bson, Bson, List<Bson>> documentStorage;
  }
``` 
- 查询一条记录
```java
/**
 * 查询一条记录
 * @param req
 * @return
 */
public Homo<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
  log.info("getUserInfo req userId {} req {}", req.getUserId(), req);
  return documentStorage
          .get(USER_LOGIC_TYPE, req.getUserId(), "data", UserDocument.class)
          .nextDo(retUserDocument -> {
            GetUserInfoResp res;
            if (retUserDocument != null) {
              res = GetUserInfoResp.newBuilder()
                      .setErrorCode(0)
                      .setErrorDesc("获取成功")
                      .setUserInfo(UserDocument.covertUserInfoToProto(retUserDocument))
                      .build();
            } else {
              res = GetUserInfoResp.newBuilder()
                      .setErrorCode(1)
                      .setErrorDesc("没有该用户信息")
                      .build();
            }
            log.info("getUserInfo res userId {} res {}", req.getUserId(), res);
            return Homo.result(res);
          }).onErrorContinue(throwable -> {
            GetUserInfoResp res = GetUserInfoResp.newBuilder()
                    .setErrorCode(2)
                    .setErrorDesc("服务器异常")
                    .build();
            log.error("getUserInfo error userId {} req {} res {}", req.getUserId(), req, res);
            return Homo.result(res);
          });
}
``` 
- 插入一条记录
```java
public Homo<CreateUserResp> createInfo(CreateUserReq req) {
  log.info("createInfo req userId {} req {}", req.getUserId(), req);
  String userId = req.getUserId();
  return documentStorage.get(USER_LOGIC_TYPE, userId, "data", UserDocument.class)
          .nextDo(newUserDocument -> {
            if (newUserDocument != null) {
              return Homo.error(new Exception("用户已存在"));

            }
            newUserDocument = UserDocument.createUser(req.getUserId());
            UserDocument finalNewUserDocument = newUserDocument;
            return documentStorage.save(USER_LOGIC_TYPE, userId, "data", newUserDocument, UserDocument.class)
                    .nextDo(updateRet ->
                            Homo.result(CreateUserResp.newBuilder().setErrorCode(0).setErrorDesc("创建成功").setUserInfo(UserDocument.covertUserInfoToProto(finalNewUserDocument)).build())
                                    .onErrorContinue(throwable -> {
                                      CreateUserResp res = CreateUserResp.newBuilder()
                                              .setErrorCode(1)
                                              .setErrorDesc(throwable.getMessage())
                                              .build();
                                      log.info("createUserInfo fail userId {} req {} res {}", userId, req, res);
                                      return Homo.result(res);
                                    })
                    );
          });

}
``` 
 
- 查询符合条件的所有记录
```java
public Homo<QueryUserInfoResp> queryUserInfo(QueryUserInfoReq req) {
  Bson filter;
  List<Bson> filters = new ArrayList<>();
  filters.add(Filters.eq("value.userId",1)); // 查找
  filter = Filters.and(filters);

  List<Bson> sorts = new ArrayList<>(); // 按顺序
  Bson ascending = Sorts.ascending("value.age");
  sorts.add(ascending);
  Bson sort = Sorts.orderBy(sorts);

  return documentStorage.query(filter, sort, 0, 0, UserDocument.class)
          .nextDo(users->{
            QueryUserInfoResp res;
            List<UserInfoPb> userInfos = new ArrayList<>();
            for (UserDocument userDocument : users) {
              userInfos.add(UserDocument.covertUserInfoToProto(userDocument));
            }
            res = QueryUserInfoResp.newBuilder()
                    .setErrorCode(0)
                    .setErrorDesc("查询成功")
                    .addAllUserInfo(userInfos)
                    .build();
            log.info("queryUserInfo res  users {}", users);
            return Homo.result(res);
          });
}
```
 
- 聚合记录。
```java
public Homo<AggregateUserInfoResp> aggregateInfo(AggregateUserInfoReq req) {
  List<Bson> aggregateList = new ArrayList();
  aggregateList.add(Aggregates.sort(Sorts.descending("value.age")));
  aggregateList.add(Aggregates.skip(1));
  aggregateList.add(Aggregates.limit(10));
  // 关联
  aggregateList.add(Aggregates.lookup("user", "value.createId", "value.userId", "create_result"));
  // 保留空数组
  UnwindOptions unwindOptions = new UnwindOptions();
  unwindOptions.preserveNullAndEmptyArrays(Boolean.TRUE);
  // 展开数组
  aggregateList.add(Aggregates.unwind("$create_result", unwindOptions));
  // 返回值处理
  Document project = new Document();
  project.put("dynamicComment", "$value");
  project.put("createUser", "$create_result.value");
  aggregateList.add(Aggregates.project(project));
  return documentStorage.aggregate(aggregateList, UserDocument.class, UserVO.class)
          .nextDo(vo->{
            AggregateUserInfoResp resp = AggregateUserInfoResp.newBuilder().build();
            return Homo.result(resp);
          });
}
```
 
## 5. 定制化
- mongo数据库可选配置
```java
public class MongoDriverProperties {
  /**
   * 数据库连接
   */
  @Value("${homo.mongo.connString:mongodb://127.0.0.1:27017}")
  private String connString;
  /**
   * 数据库名
   */
  @Value("${homo.mongo.database:homo_storage}")
  private String database;
  /**
   * 连接池最小数量
   */
  @Value("${homo.mongo.minSize:1}")
  private Integer minSize;
  /**
   * 连接池最大数量
   */
  @Value("${homo.mongo.maxSize:100}")
  private Integer maxSize;
  @Value("${homo.mongo.maxWaitTime:100}")
  /**
   * 最大请求等待时间
   */
  private Long maxWaitTime;
  @Value("${homo.mongo.maxConnectionIdleTime:10000}")
  /**
   * 最长空闲等待时间
   */
  private Long maxConnectionIdleTime;
  /**
   * 最大连接存活时间
   */
  @Value("${homo.mongo.maxConnectionLifeTime:60000}")
  private Long maxConnectionLifeTime;
  /**
   * 重试写，默认开启（mongo默认配置，会有事务）
   */
  @Value("${homo.mongo.retryWrites:true}")
  private Boolean retryWrites;
  /**
   * 读偏好，有primary（只从主节点读），secondary（只从从节点读），primaryPreferred（优先从主节点读），secondaryPreferred（优先从从节点读），nearest（从最近的主机读）
   */
  @Value("${homo.mongo.readPreference:primary}")
  private String readPreference;
  /**
   * 写关注，有UNACKNOWLEDGED（不确认，最快，但最不安全），ACKNOWLEDGED（所有节点都确认，默认配置），JOURNALED（写入JOURNAL日志后确认），MAJORITY（大部分节点确认），W1（一个节点确认），W2，W3
   */
  @Value("${homo.mongo.writeConcern:ACKNOWLEDGED}")
  private String writeConcern;
}
``` 
 