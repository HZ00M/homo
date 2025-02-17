
# Storage

## 1. 概述

- **homo框架Storage存储驱动**: 
- **版本**: [版本号，homo-core >= 1.0]
- **功能简介**:
    - 为homo框架提供统一的对象存储能力，如Entity数据存储、服务状态存储、负载均衡存储
    - 基于storageDriver的数据操作不直接操作数据库，而是基于对一级缓存的更新，并通过dirtyDriver记录这些更新，有landingDriver进行数据落地
- **功能目标**: 
  - 提供统一的存储sdk、统一的表结构
  - 提供数据一级缓存能力
  - 提供自动落地存储服务

```text
/**
 * 设计思路
 * 1数据会按照指定格式进行存储到hSet中，该hSet称之为ownerKey,其中的filed称之为logicKey, 其中一个cachedAllKey（field）标识该key上的所有field都已加载进内存中
 * 存储流程：先存redis，再由另一台落地程序定时将redis上的数据落地到mysql
 * 存储规则： 当调用update或incr更新数据时，会在redis上打个不过期的string类型标签，标志该key
 * 存在在mysql（existKey）existKey用于判断数据是否是存在的
 * 2当调用get方法获取数据时，如果redis没有数据，会通过existKey判定是否要从mysql加载数据，
 * 3如果存在existKey,就进行数据加热操作（hotkey），不存在则直接返回
 * 4从mysql获取到数据后，会将其存入到redis中（数据结构是hSet），然后重新执行get方法从redis获取数据.
 * 与此同时会在数据结构上增加一个field标识(成员名为cacheKey)，cacheKey用于判断数据是否存在于redis，存在才会从redis获取数据
 * 如果cacheKey存在，就从redis捞取后返回，如果不存在，则执行步骤3
 * 5数据移除会将需要移除的数据迁移到hSet的另一个字段上（logicKey+:+del）（逻辑删除）,然后原先的logicKey的值会被打上删除标记（:delFlag标识）
 */
```
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

## 3. 核心模块
---
### 存储
- ByteStorage 提供bytes类型的StorageDriver调用的封装 
- ObjStorage 提供ByteStorage进一步封装，用于对象类型的存储
- StorageDriver 提供存储能力
```java
public interface StorageDriver extends Driver {
    String REDIS_KEY_TMPL = "slug-data:{%s:%s:%s:%s}";
    /**
     * 通过key列表获取value
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param fieldList key列表
     */
    Homo<Map<String, byte[]>> asyncGetByFields(String appId, String regionId, String logicType, String ownerId, List<String> fieldList);

    /**
     * 获得所有key 和 value
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     */
    Homo<Map<String, byte[]>> asyncGetAll(String appId, String regionId, String logicType, String ownerId);
    /**
     * 更新多key,value数据，通过回调返回详细结果
     *
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param data 待保存数据
     */
    Homo<Pair<Boolean, Map<String, byte[]>>> asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, byte[]> data);

    /**
     * 增加key列表的值
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param incrData 指定key的值列表
     */
    Homo<Pair<Boolean, Map<String, Long>>> asyncIncr(String appId, String regionId, String logicType, String ownerId, Map<String, Long> incrData);
    /**
     * 删除key列表的值(逻辑删除)
     * @param appId     appid
     * @param regionId  regionId
     * @param logicType 逻辑类型
     * @param ownerId   ID
     * @param remKeys 指定key的值列表
     */
    Homo<Boolean> asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys);
}
``` 
### 脏数据处理
- DirtyDriver 提供脏数据处理逻辑，脏数据即操作StorageDriver时产生的数据更新，脏数据通过DirtyDriver进行记录
```java
/**
 * 设计思路
 * 1 脏表逻辑
 *   当使用storage进行更新(更新包括增加、更新、自增、删除)操作时，将hset对应的filed的key保存在一张待更新的dirtyMap（hset结构中）中，
 *   这个hset结构需要有多个，用户的更新操作按照一定规则（按照key进行hash）随机存储在任一个脏表结构中（dirtyMap）,等待落地程序执行
 *   落地操作（PersistentDriver负责根据脏表的持久化操作）


 /**
 * 2 落地逻辑
 *   一个落地程序只负责一定数量的脏表（dirtyMap）的持久化操作，在执行持久化前会判断dirtyMap是否已经有其他落地程序负责
 *   在获取脏表的持久化权限后，起一个定时程序进行定时落地操作，落地前会将当前脏表进行重命名操作（相当于快照操作，不与正在更新
 *   的资源同时操作同一个hset），然后每次落地会按照配置的落地数去该脏表执行Hscan指定数量的key,
 *   然后根据这些key去redis中拿到对应的数据，构造出批量更新的sql，然后执行sql进行落地，
 *   批量落地如果失败了。切换成单个更新模式，单个更新失败了，切换成失败模式（将失败的key存在另一个errorMap的异常表中hset）
 *   循环执行上述落地流程
 */
public interface DirtyDriver {
  /**
   * hset操作,存脏表数据
   */
  Homo<Long> dirtyUpdate(Dirty dirty);

  String chooseDirtyMap() ;

  Boolean lockDirtyMap(String dirtyName);

  Boolean unlockDirtyMap(String dirtyName);
  /**
   * 迭代获得脏表的数据
   * @param key
   * @param index
   * @param count
   * @return
   */
  List getDirtyList(String key, String index, String count);

  /**
   * 修改脏表的名字
   * @param dirtyName
   * @return
   */
  String snapShot(String dirtyName);


  boolean landing(String dirtyTableName, String dirtySavingTableName);
}
``` 
### 落地驱动
- LandingDriver 提供对数据库冷数据的捞取和对热数据的落地处理
```java
/**
 * 落地驱动
 * 提供数据捞取，落地服务
 */
public interface LandingDriver<T> {
  /**
   * 对指定hset的所有key捞出。通过该接口将数据库的冷数据进行热加载进一级缓存
   * @param appId
   * @param regionId
   * @param logicType
   * @param ownerId
   * @param redisKey
   * @return
   */
  Homo<Boolean> hotAllField(String appId, String regionId, String logicType, String ownerId, String redisKey);

  /**
   * 对指定hset的指定key捞出。通过该接口将数据库的冷数据进行热加载进一级缓存
   * @param appId
   * @param regionId
   * @param logicType
   * @param ownerId
   * @param redisKey
   * @return
   */
  Homo<List<T>> hotFields(String appId, String regionId, String logicType, String ownerId, String redisKey, List<String> fields);

  /**
   * 通过该方法将dirtyDriver的脏数据落地到数据库
   * @param dirtyTableName
   * @param dirtyList
   * @return
   */
  boolean batchLanding(String dirtyTableName, List<String> dirtyList);

  /**
   * 通过该方法将dirtyDriver的脏数据落地到数据库
   * @param dirtyList
   * @param dirtyList
   * @return
   */
  boolean singleLanding(List<String> dirtyList, String dirtyName);
}

```
