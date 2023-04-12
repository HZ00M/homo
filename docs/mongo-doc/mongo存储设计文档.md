# mongo存储设计说明文档
## 简介
```text
    homo框架mongo存储驱动
```

## 目的
- 提供mongo数据库存储支持
- 屏蔽底层存储细节
## 前提
- 基于homo-core的存储框架
## 版本
- homo-core >= 1.0
##接口设计
```java
public interface EntityStorageDriver<F, S, U, P> extends Driver {

    /**
     * 查询文档数据
     *
     * @param filter 过滤条件
     * @param sort   排序条件
     * @param limit  limit
     * @param skip   skip
     * @param clazz  文档对象类型
     * @param callBack    回调返回结果
     */
    <T> void asyncQuery(F filter, S sort,@NotNull Integer limit, Integer skip, Class<T> clazz, CallBack<List<T>> callBack);

    /**
     * 查询文档数据，返回视图
     *
     * @param filter     过滤条件
     * @param viewFilter 视图过滤条件
     * @param sort       排序条件
     * @param limit      limit
     * @param skip       skip
     * @param clazz      文档对象类型
     * @param callBack        回调返回结果
     */
    <T, V> void asyncQuery(F filter, F viewFilter, S sort, @NotNull Integer limit, Integer skip, Class<V> viewClazz, Class<T> clazz, CallBack<List<V>> callBack);

    /**
     * 查找并修改
     *
     * @param filter 过滤条件
     * @param clazz 文档对象类型
     * @param callBack 回调返回结果
     */
    <T> void asyncFindAndModify(String logicType, String ownerId,String key,F filter, U update, Class<T> clazz, CallBack<Boolean> callBack);

    /**
     * 异步聚合
     * @param pipeLine 聚合管道
     * @param viewClazz 返回结果视图
     * @param clazz 文档对象类型
     * @param callBack 回调返回结果
     */
    <T, V> void asyncAggregate(P pipeLine, Class<V> viewClazz, Class<T> clazz, CallBack<List<V>> callBack);

    /**
     * 通过key列表获取value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param keyList   key列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回值列表
     */
    <T> void asyncGetByKeys(String appId, String regionId, String logicType, String ownerId, List<String> keyList, Class<T> clazz, CallBack<Map<String, T>> callBack);

    /**
     * 获得同一路径下所有key 和 value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param clazz     文档对象类型
     * @param callBack       回调返回结果
     */
    <T> void asyncGetAll(String appId, String regionId, String logicType, String ownerId, Class<T> clazz, CallBack<Map<String, T>> callBack);

    /**
     * 更新多key多value数据（全量更新数据），通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    <T> void asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, T> data, Class<T> clazz, CallBack<Pair<Boolean, Map<String, T>>> callBack);

    /**
     * 更新单key的value数据（增量更新数据），通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data      待保存数据（更新其非空字段）
     * @param clazz     文档对象类型
     * @param callBack       回调详细结果
     */
    <T> void asyncUpdatePartial(String appId, String regionId, String logicType, String ownerId, String key, Map<String, ?> data, Class<T> clazz, CallBack<Boolean> callBack);


    /**
     * 增加key列表的值
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData  指定key的值列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回新值列表
     */
    <T> void asyncIncr(String appId, String regionId, String logicType, String ownerId, String key, Map<String, Long> incrData, Class<T> clazz, CallBack<Pair<Boolean, Map<String, Long>>> callBack);

    /**
     * 逻辑删除key列表的值
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys   指定key的值列表
     * @param clazz     文档对象类型
     * @param callBack       回调返回是否删除成功
     */
    <T> void asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, Class<T> clazz, CallBack<Boolean> callBack);

    /**
     * 查询集合数量
     *
     * @param clazz 文档对象类型
     * @param rel   回调返回结果
     */
    <T> void getCount(F filter, int limit, int skip, String hint, Class<T> clazz, CallBack<Long> rel);
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Document {
    /**
     * 文档名 
     */
    String collectionName() ;

    /**
     * 索引列表
     * @return
     */
    String[] indexes() default {};

    IndexType indexType() default IndexType.ASC;

    enum IndexType{
        ASC,
        DESC
    }
}
```
##用例
- 定义文档实体
```java
@Log4j2
@BsonDiscriminator
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collectionName = "user", indexes = {"value.nickName", "value.bindGameList.gameName", "value.level", "value.registerTime", "value.tagList"})
public class User implements Serializable {
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

    private Long deadlineDay; // 成长值最后期限

    private Map<String, BindGame> bindGameMap;                          //已绑定角色列表
    private List<Address> addressList;                                  //地址列表
    private List<String> tagList = new ArrayList() {{                    //用户标签
        add("");
        add("");
        add("");
        add("");
        add("");
        add("");
        add("");
        add("");
    }};
    //省略逻辑代码
}
```
- 定义服务类
```java
@Component
@Log4j2
public class AppUserServiceImpl implements AppUserService {
    /**
     * 使用响应式Mongo文档存储组件
     */
    @Autowired
    RectorEntityStorage<Bson, Bson, Bson, List<Bson>> entityStorage;

    /**
     * 实现业务逻辑
     * @param req
     * @return
     */
    public Homo<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        log.info("getUserInfo req userId_{} req_{}", req.getUserId(), req);
        return entityStorage
                .get(LogicType.USER.name(), req.getUserId(), "data", User.class)
                .nextDo(retUser -> {
                    GetUserInfoResp res;
                    if (retUser != null) {
                        res = GetUserInfoResp.newBuilder()
                                .setErrorCode(0)
                                .setErrorDesc("获取成功")
                                .setUserInfo(User.covertUserInfoToProto(retUser))
                                .build();
                    } else {
                        res = GetUserInfoResp.newBuilder()
                                .setErrorCode(1)
                                .setErrorDesc("没有该用户信息")
                                .build();
                    }
                    log.info("getUserInfo res userId_{} res_{}", req.getUserId(), res);
                    return Homo.result(res);
                }).onErrorContinue(throwable -> {
                    GetUserInfoResp res = GetUserInfoResp.newBuilder()
                            .setErrorCode(2)
                            .setErrorDesc("服务器异常")
                            .build();
                    log.error("getUserInfo error userId_{} req_{} res_{}", req.getUserId(), req, res);
                    return Homo.result(res);
                });
    }

    @Override
    public Homo<CreateUserResp> createInfo(CreateUserReq req) {
        log.info("createUserInfo req userId_{} req_{}", req.getUserInfo().getUserId(), req);
        String userId = req.getUserInfo().getUserId();
        String uuid = UUID.randomUUID().toString();
        return entityStorage
                .asyncLock(LogicType.USER.name(), userId, "user", "", uuid, 5, 5, 1)
                .nextDo(aBoolean -> {
                    if (!aBoolean) {
                        log.error("createUserInfo lock fail userId_{} ", userId);
                        return Homo.error(new Error());
                    }
                    log.info("createUserInfo lock success userId_{} ", userId);
                    return Homo.result(1);
                })
                .nextDo(ret ->
                        entityStorage.get(LogicType.USER.name(), userId, "data", User.class)
                                .nextDo(newUser -> {
                                    if (newUser == null) {
                                        boolean isPass = User.checkCreateInfo(req.getUserInfo());
                                        if (!isPass) {
                                            return Homo.error(new Exception("参数错误"));
                                        }
                                        newUser = User.createUser(req.getUserInfo());
                                        return entityStorage.save(LogicType.USER.name(), userId, "data", newUser, User.class);
                                    }
                                    return Homo.error(new Exception("用户已存在"));
                                })
                                .nextDo(saveUser -> Homo.result(CreateUserResp.newBuilder().setErrorCode(0).setErrorDesc("创建成功").setUserInfo(User.covertUserInfoToProto(saveUser)).build())
                                        .onErrorContinue(throwable -> {
                                            CreateUserResp res = CreateUserResp.newBuilder()
                                                    .setErrorCode(1)
                                                    .setErrorDesc(throwable.getMessage())
                                                    .build();
                                            log.info("createUserInfo fail userId_{} req_{} res_{}", userId, req, res);
                                            return Homo.result(res);
                                        })
                                )
                                .nextDo(resp ->
                                        entityStorage
                                                .asyncUnlock(LogicType.USER.name(), userId, "user", uuid)
                                                .nextDo(unlock -> Homo.result(resp))
                                )
                );
    }
}
```
##定制化
- mongo连接配置
```java
@Configurable
@Data
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
