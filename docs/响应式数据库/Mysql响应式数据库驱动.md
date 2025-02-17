
# 基于Mysql的响应式驱动

## 1. 概述

- **功能目标**: 
  - Mysql的响应式驱动实现
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
 
application.properties配置如下，其中namespace是homo_relational_mysql的配置响应式关系库的相关信息，用户也可创建自己自定义的namespace
```text
  apollo.bootstrap.enabled = true
  apollo.bootstrap.namespaces = application,homo_redis_config,redis_connect_info,homo_root_info,homo_zipkin_config,homo_relational_mysql
  spring.main.web-application-type=none
```
homo_relational_mysql参考配置
```text
  homo.datasource.url = jdbc:mysql://homo-mysql:30006/homo
  homo.datasource.host = 10.100.1.236
  homo.datasource.username = root
  homo.datasource.password = password
  homo.relational.mysql.connect.port = 30006
  homo.relational.mysql.connect.host = 10.100.1.236
  homo.relational.mysql.connect.database = homo
  homo.relational.mysql.connect.username = root
  homo.relational.mysql.connect.password = password
  homo.relational.mysql.connect.url = r2dbc:mysql://homo-mysql:30006/homo?serverTimezone=Asia/Shanghai
```
- **支持的操作系统**:
    - Windows 10 或更高
    - macOS 10.13 或更高
    - Linux (Ubuntu 18.04+)
---

## 3. 使用指南

### 3.1 添加依赖
在项目的 `pom.xml` 文件中添加以下依赖：
-  开启响应式数据库功能：homo-core-relational-base
-  使用mysql驱动：homo-core-relational-driver-mysql
```xml 
    <dependencies>
    <dependency>
      <groupId>com.homo</groupId>
      <artifactId>homo-core-relational-base</artifactId>
    </dependency>
    <dependency>
      <groupId>com.homo</groupId>
      <artifactId>homo-core-relational-driver-mysql</artifactId>
    </dependency>
  </dependencies>
```
 

## 4. 快速入门
以下是一个简单的示例代码，展示如何使用如何使用sdk操作数据：
- 创建一个homo-relational-mysql-demo,并添加相关依赖
```xml
    <dependencies>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-relational-mysql-facade-demo</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-relational-base</artifactId>
  </dependency>
  <dependency>
    <groupId>com.homo</groupId>
    <artifactId>homo-core-relational-driver-mysql</artifactId>
  </dependency> 
</dependencies>
```
- 创建一个DrawCardTable表定义类，使用@HomoTable声明表定义信息，@HomoIndex定义索引信息，@HomoId声明主键，@HomoColumn定义列信息，如下
```java
@HomoTable(value = "draw_card_record",
        indices = {@HomoIndex(columns = {"user_id","pool_id"},indexType = HomoIndex.IndexType.NORMAL)})
public class DrawCardTable {
    @HomoId(autoGenerate = true)
    public long id;
    /**
     * 玩家UID
     */
    @HomoColumn(value = "user_id")
    public String userId;

    /**
     * 卡池ID
     */
    @HomoColumn(value = "pool_id")
    public int poolId;
}
```
对应的数据库结构如下
```mysql
CREATE TABLE `draw_card_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `pool_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `nr_user_id_pool_id` (`user_id`(191),`pool_id`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4;


``` 
以下是一个简单的示例代码，展示如何使用RelationalTemplate进行数据操作：
  - 业务bean中注入RelationalTemplate
```java
@Component
@Slf4j
public class DrawCardService extends BaseService implements DrawCardFacade {
  @Autowired
  RelationalTemplate relationalTemplate;
  }
``` 
- 保存一条记录
```java
   public Homo<SaveDrawCardResp> save(SaveDrawCardReq req, HttpHeadInfo header) {
    DrawCardPb drawCard = req.getDrawCard();
    int poolId = drawCard.getPoolId();
    String userId = drawCard.getUserId();
    DrawCardTable drawCardTable = new DrawCardTable();
    drawCardTable.poolId = poolId;
    drawCardTable.userId = userId;
    log.info("save start drawCardTable {}", drawCardTable);
    return relationalTemplate.save(DrawCardTable.class).value(drawCardTable)
            .nextDo(ret -> {
              log.info("save success drawCardTable {} ret {}", drawCardTable, ret);
              return Homo.result(SaveDrawCardResp.newBuilder().setCode(1).setId(ret.getId()).build());
            });
}
``` 
- 插入一条记录
```java
    public Homo<InsertDrawCardResp> insert(InsertDrawCardReq req) {
    DrawCardPb drawCardPb = req.getDrawCard();
    DrawCardTable drawCardTable = new DrawCardTable();
    drawCardTable.id = drawCardPb.getId();
    drawCardTable.poolId = drawCardPb.getPoolId();
    drawCardTable.userId = drawCardPb.getUserId();
    log.info("insert start drawCardTable {} ", drawCardTable);
    return relationalTemplate.insert(DrawCardTable.class).value(drawCardTable)
            .nextDo(ret -> {
              log.info("insert success, drawCardTable {}  ret {}", drawCardTable, ret);
              return Homo.result(InsertDrawCardResp.newBuilder().setCode(1).setId(ret.getId()).build());
            });
}
``` 
- 插入多条记录
```java
    public Homo<InsertsDrawCardResp> inserts(InsertsDrawCardReq req) {
        List<DrawCardTable> list = new ArrayList<>();
        for (DrawCardPb drawCardPb : req.getDrawCardList()) {
            DrawCardTable drawCardTable = new DrawCardTable();
            drawCardTable.id = drawCardPb.getId();
            drawCardTable.poolId = drawCardPb.getPoolId();
            drawCardTable.userId = drawCardPb.getUserId();
            list.add(drawCardTable);
        }
        DrawCardTable[] array = list.toArray(new DrawCardTable[0]);
        return relationalTemplate.insert(DrawCardTable.class).values(array)
                .nextDo(ret -> {
                    log.info("insert success, array {} ret {}", array, ret);
                    return Homo.result(InsertsDrawCardResp.newBuilder().setCode(1).build());
                });
    }
```
- 查询所有id in (id1,id2) and pool_id = 1 的记录
```java
    public Homo<QueryDrawCardResp> queryFindAll(QueryDrawCardReq req) {
        List<Long> idsList = req.getIdsList();
        log.info("queryFindAll start idsList {}", idsList);
        HomoCriteria criteria = HomoCriteria.where("id").in(idsList.toArray())
                .and("pool_id").eq("1");
        return relationalTemplate.find(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                                .sort(HomoSort.by(HomoSort.Order.desc("id"), HomoSort.Order.desc("pool_id")))
                                .limit(2)
                                .offset(1)
                )
                .findAll()
                .nextDo(ret -> {
                    log.info("queryFindAll success, ret {}", ret);
                    List<DrawCardPb> drawCardPbList = ret.stream().map(DrawCardTable::covertToPb).collect(Collectors.toList());
                    QueryDrawCardResp drawCardResp = QueryDrawCardResp.newBuilder().addAllDrawCards(drawCardPbList).build();
                    return Homo.result(drawCardResp);
                });
    }
```
- 查询第一条id = id1的记录
```java
    public Homo<QueryDrawCardResp> queryFindOne(QueryDrawCardReq req) {
      List<Long> idsList = req.getIdsList();
      log.info("queryFindOne start idsList {}", idsList);
      HomoCriteria criteria = HomoCriteria.where("id").eq(idsList.get(0));
      return relationalTemplate.find(DrawCardTable.class)
              .matching(
                      HomoQuery.query(criteria)
                              .sort(HomoSort.by(HomoSort.Order.asc("id"), HomoSort.Order.desc("pool_id")))
              )
              .findOne()
              .nextDo(ret -> {
                log.info("queryFindOne success, ret {}", ret);
                DrawCardPb drawCardPb = ret.covertToPb();
                QueryDrawCardResp drawCardResp = QueryDrawCardResp.newBuilder().addDrawCards(drawCardPb).build();
                return Homo.result(drawCardResp);
              });
}
```
- 判断user_id in (id1,id2)的记录是否存在
```java
    public Homo<QueryDrawCardResp> queryFindExists(QueryDrawCardReq req) {
        List<Long> idsList = req.getIdsList();
        log.info("queryFindExists start idsList {}", idsList);
        HomoCriteria criteria = HomoCriteria.where("user_id").in(idsList.toArray());
        return relationalTemplate.find(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                                .sort(HomoSort.by(HomoSort.Order.asc("id"), HomoSort.Order.desc("pool_id")))
                )
                .exists()
                .nextDo(ret -> {
                    log.info("queryFindExists success, ret {}", ret);
                    QueryDrawCardResp drawCardResp = QueryDrawCardResp.newBuilder().build();
                    return Homo.result(drawCardResp);
                });
    }
```
- 删除user_id in (id1,id2)的记录
```java
    public Homo<DeleteDrawCardResp> delete(DeleteDrawCardReq req) {
        log.info("delete start, req {}", req);
        List<Long> idsList = req.getIdsList();
        HomoCriteria criteria = HomoCriteria.where("user_id").in(idsList.toArray());
        return relationalTemplate
                .delete(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                )
                .all()
                .nextDo(ret -> {
                    log.info("delete success, ret {}", ret);
                    return Homo.result(DeleteDrawCardResp.newBuilder().setCode(0).build());
                });
    }
```
- 通过对象更新符合条件的记录
```java
    public Homo<UpdateDrawCardResp> updateEntity(UpdateDrawCardReq req) {
        log.info("updateEntity start, req {}", req);
        DrawCardTable drawCardTable = DrawCardTable.coverPbTo(req.getDrawCard());
        HomoCriteria criteria = HomoCriteria.where("id").eq(drawCardTable.getId());
        return relationalTemplate
                .update(DrawCardTable.class)
                .matching(
                        HomoQuery.query(criteria)
                )
                .apply(drawCardTable)
                .nextDo(ret -> {
                    log.info("updateEntity success, ret {}", ret);
                    return Homo.result(UpdateDrawCardResp.newBuilder().setCode(1).build());
                });
    }
```
- 通过HomoUpdate更新符合条件的记录
```java
    public Homo<UpdateDrawCardResp> update(UpdateDrawCardReq req) {
      log.info("update start, req {}", req);
      DrawCardTable drawCardTable = DrawCardTable.coverPbTo(req.getDrawCard());
      HomoCriteria criteria = HomoCriteria.where("id").eq(drawCardTable.getId());
      HomoUpdate homoUpdate = HomoUpdate.builder().set("pool_id", drawCardTable.getPoolId()).build();
      return relationalTemplate
              .update(DrawCardTable.class)
              .matching(
                      HomoQuery.query(criteria)
              )
              .apply(homoUpdate)
              .nextDo(ret -> {
                log.info("update success, ret {}", ret);
                return Homo.result(UpdateDrawCardResp.newBuilder().setCode(1).build());
              });
    }
```
- 执行sql
```java
    public Homo<ExecuteSqlResp> execute(ExecuteSqlReq req) {
        String sql = req.getSql();
        return relationalTemplate
                .execute(sql)
                .all()
                .nextDo(ret -> {
                    log.info("execute success, ret {}", ret);
                    return Homo.result(ExecuteSqlResp.newBuilder().setCode(1).build());
                });
    }
```
- 聚合操作按 user_id 分组，计算每个用户的 pool_id 以别名（sum_pool_id_1）返回，同时计算整个表的 pool_id 以别名总和（sum_pool_id_2），并筛选 id > 1 的记录。
```java
    public Homo<AggregateResp> aggregate(AggregateReq req) {
        HomoAggregation aggregation = HomoAggregation.newBuilder(DrawCardTable.class)
                .project("user_id")
                .group(GroupOp.create("user_id").sum("pool_id").as("sum_pool_id_1"))
                .sum("pool_id", "sum_pool_id_2")
                .match(HomoCriteria.where("id").greaterThan("1"))
                .build();
        return relationalTemplate.aggregate(aggregation, DrawCardVO.class)
                .all()
                .nextDo(ret -> {
                    log.info("aggregate success, ret {}", ret);
                    return Homo.result(AggregateResp.newBuilder().setCode(1).build());
                });
    }
```
- 聚合操作投影 user_id、pool_id、pool_name、user_name，然后通过 pool_id 关联 pool_record 表、通过 user_id 关联 user_record 表，筛选 pool_id 为 1 或 2 的记录，并限制返回 4 条数据。
```java
    public Homo<AggregateResp> lookUp(AggregateReq req) {
        HomoAggregation aggregation = HomoAggregation.newBuilder(DrawCardTable.class)
                .project(ProjectOp.New().andProject("user_id").andProject("pool_id")
                        .andProject("name", "pool_name").andProject("user_name"))
                .lookup("pool_record", "pool_id", "id", "key", "name")
                .lookup("user_record", "user_id", "id", "key2", "user_name")
                .match(HomoCriteria.where("pool_id").eq(1).or("pool_id").eq(2))
                .limit(4)
                .build();
        return relationalTemplate.aggregate(aggregation, LookUpVO.class)
                .all()
                .nextDo(ret -> {
                    log.info("aggregate success, ret {}", ret);
                    return Homo.result(AggregateResp.newBuilder().setCode(1).build());
                });
    }
```
## 5. 定制化
- mysql 响应式数据库可选配置
```java
  /**
* MySQL 关系型数据库的配置信息
  */
  public class RelationalMysqlProperties implements InitializingBean {
    /** MySQL 连接端口 */
    @Value("${homo.relational.mysql.connect.port:}")
    private int port;

    /** MySQL 连接主机地址 */
    @Value("${homo.relational.mysql.connect.host:}")
    private String host;

    /** 连接的数据库名称 */
    @Value("${homo.relational.mysql.connect.database:}")
    private String database;

    /**
     * 数据库驱动类型（默认值为 "mysql"）
     * 可用于区分不同数据库类型，如 MySQL、PostgreSQL 等。
     */
    @Value("${homo.relational.mysql.connect.driver:mysql}")
    private String driver;

    /**
     * MySQL 连接 URL
     * 示例:
     * - JDBC: jdbc:mysql://mysql:30006/tpf_storage?Unicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
     * - R2DBC: r2dbc:mysql://mysql:30006/tpf_storage?useSSL=false&characterEncoding=UTF-8
     */
    @Value("${homo.relational.mysql.connect.url:}")
    private String url;

    /** MySQL 连接用户名 */
    @Value("${homo.relational.mysql.connect.username:}")
    private String username;

    /** MySQL 连接密码 */
    @Value("${homo.relational.mysql.connect.password:}")
    private String password;

    /**
     * 连接超时时间（单位：秒，默认值 3）
     * 表示数据库连接的超时等待时间。
     */
    @Value("${homo.relational.mysql.connect.timeout.second:3}")
    private Integer timeoutSecond;

    /**
     * 数据库表名前缀（默认值 "homo_storage"）
     * 可用于多租户架构下的数据库表命名规范。
     */
    @Value("${homo.relational.mysql.database.prefix:homo_storage}")
    private String prefix;
}
``` 
 