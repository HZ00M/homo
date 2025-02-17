
# 文档型数据库驱动

## 1. 概述

- **homo框架响应式Document数据库操作组件**: 
- **版本**: [版本号，homo-core >= 1.0]
- **功能简介**:
    - 响应式数据库基于响应式编程（Reactive Programming）的理念，能够更高效地处理异步数据流，特别适用于高并发、低延迟、流式数据处理等场景
- **功能目标**: 
  - 提供响应式文档型数据库操作sdk
  - 屏蔽底层数据库类型 
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
### 表定义相关功能
- @Document 定义表注解
```java
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
 
### 数据操作相关功能
- DocumentStorage 提供DocumentStorageDriver调用的封装
- DocumentStorageDriver 定义底层驱动能力
```java
public interface DocumentStorageDriver<F, S, U, P> extends Driver {

  /**
   * 查询文档数据
   *
   * @param filter 过滤条件
   * @param sort   排序条件
   * @param limit  limit
   * @param skip   skip
   * @param clazz  文档对象类型
   */
  <T> Homo<List<T>> asyncQuery(F filter, S sort, @NotNull Integer limit, Integer skip, Class<T> clazz);

  /**
   * 查询文档数据，返回视图
   *
   * @param filter     过滤条件
   * @param viewFilter 视图过滤条件
   * @param sort       排序条件
   * @param limit      limit
   * @param skip       skip
   * @param clazz      文档对象类型
   */
  <T, V> Homo<List<V>> asyncQuery(F filter, F viewFilter, S sort, @NotNull Integer limit, Integer skip, Class<V> viewClazz, Class<T> clazz);

  /**
   * 查找并修改
   *
   * @param filter 过滤条件
   * @param clazz 文档对象类型
   */
  <T> Homo<Boolean> asyncFindAndModify(String logicType, String ownerId,String key,F filter, U update, Class<T> clazz);

  /**
   * 异步聚合
   * @param pipeLine 聚合管道
   * @param viewClazz 返回结果视图
   * @param clazz 文档对象类型
   */
  <T, V> Homo<List<V>> asyncAggregate(P pipeLine, Class<V> viewClazz, Class<T> clazz);

  /**
   * 通过key列表获取value
   *
   * @param appId     appid
   * @param regionId  regionId
   * @param logicType 逻辑类型
   * @param ownerId   ID
   * @param keyList   key列表
   * @param clazz     文档对象类型
   */
  <T> Homo<Map<String, T>> asyncGetByKeys(String appId, String regionId, String logicType, String ownerId, List<String> keyList, Class<T> clazz);

  /**
   * 获得同一路径下所有key 和 value
   *
   * @param appId     appid
   * @param regionId  regionId
   * @param logicType 逻辑类型
   * @param ownerId   ID
   * @param clazz     文档对象类型
   */
  <T> Homo<Map<String, T>> asyncGetAll(String appId, String regionId, String logicType, String ownerId, Class<T> clazz);

  /**
   * 更新多key多value数据（全量更新数据），通过回调返回详细结果
   *
   * @param appId     appid
   * @param regionId  regionId
   * @param logicType 逻辑类型
   * @param ownerId   ID
   * @param data      待保存数据
   * @param clazz     文档对象类型
   */
  <T> Homo<Pair<Boolean, Map<String, T>>> asyncUpdate(String appId, String regionId, String logicType, String ownerId, Map<String, T> data, Class<T> clazz);

  /**
   * 更新单key的value数据（增量更新数据），通过回调返回详细结果
   *
   * @param appId     appid
   * @param regionId  regionId
   * @param logicType 逻辑类型
   * @param ownerId   ID
   * @param data      待保存数据（更新其非空字段）
   * @param clazz     文档对象类型
   */
  <T> Homo<Boolean> asyncUpdatePartial(String appId, String regionId, String logicType, String ownerId, String key, Map<String, ?> data, Class<T> clazz);


  /**
   * 增加key列表的值
   *
   * @param appId     appid
   * @param regionId  regionId
   * @param logicType 逻辑类型
   * @param ownerId   ID
   * @param incrData  指定key的值列表
   * @param clazz     文档对象类型 
   */
  <T> Homo<Pair<Boolean, Map<String, Long>>> asyncIncr(String appId, String regionId, String logicType, String ownerId, String key, Map<String, Long> incrData, Class<T> clazz);

  /**
   * 逻辑删除key列表的值
   *
   * @param appId     appid
   * @param regionId  regionId
   * @param logicType 逻辑类型
   * @param ownerId   ID
   * @param remKeys   指定key的值列表
   * @param clazz     文档对象类型
   */
  <T> Homo<Boolean> asyncRemoveKeys(String appId, String regionId, String logicType, String ownerId, List<String> remKeys, Class<T> clazz);

  /**
   * 查询集合数量
   *
   * @param clazz 文档对象类型
   */
  <T> Homo<Long> getCount(F filter, int limit, int skip, String hint, Class<T> clazz);
}

```
  
 