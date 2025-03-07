
# 响应式数据库

## 1. 概述

- **homo框架响应式数据库组件**: 
- **版本**: [版本号，homo-core >= 1.0]
- **功能简介**:
    - 响应式数据库基于响应式编程（Reactive Programming）的理念，能够更高效地处理异步数据流，特别适用于高并发、低延迟、流式数据处理等场景
- **功能目标**: 
  - 提供响应式数据库操作sdk
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
- @HomoTable 定义表注解
  ```java
  public @interface HomoTable {
  
    /**
     * 表名 空时默认类名
     */
    String value() default "";
  
    /**
     * 自动生成表
     * @return
     */
    boolean generate() default true;
  
    /**
     * 索引
     * @return
     */
    HomoIndex[] indices() default {};
  
    /**
     * 分表策略
     * @return
     */
    Class<? extends HomoTableDivideStrategy> nameStrategy() default DefaultHomoTableDivideStrategy.class;
  
    String driverName() default "";
  }
  
  
  ``` 
- @HomoIndex 定义索引注解
  ```java
  public @interface HomoIndex {
    /**
     * 索引名，为空时根据字段名拼接
     * @return
     */
    String name() default "";
  
    /**
     * 索引相关的项
     * @return
     */
    String[] columns();
  
    IndexType indexType() default IndexType.NORMAL;
  
    enum IndexType {
      UNIQUE,
      NORMAL,
    }
  
  }
  
  ```
- @HomoColumn 列定义注解
  ```java
  public @interface HomoColumn {
    /**
     * column name
     */
    String value() default "";
  
    /**
     * column length
     */
    int length() default 255;
  
    /**
     * column default value
     */
    String defaultValue() default "";
  
    /**
     * column is nullable
     */
    boolean nullable() default true;
  
    /**
     * double时表示数值的总长度
     */
    int precision() default 0;
  
    /**
     * float时表示小数点所占的位数
     */
    int scale() default 0;
  }
  
  ```
### 数据操作相关功能
- RelationalTemplate 响应式数据库操作模板类
  ```java
  /**
   * 所有关系型的操作接口
   */
  public interface RelationalTemplate<P extends AggregateOperation.Aggregation> extends InsertOperation,SelectOperation,UpdateOperation,DeleteOperation,ExecuteOperation,AggregateOperation<P>{
    default String driverName(){
      return "";
    }
  }
  
  ```
- InsertOperation 插入相关操作
  ```java
  /**
   * 数据保存操作接口
   */
  public interface InsertOperation {
    /**
     * 保存一个对象，如果这个对象存在就更新这个对象
     */
    <T> InsertSpec<T> save(Class<T> domainType, Object... args);
  
    /**
     * 插入一个对象，如果对象存在即失败
     * @param <T>
     */
    <T> InsertSpec<T> insert(Class<T> domainType, Object... args);
  
    /**
     * 插入一个对象，如果对象存在即忽略
     * @param <T>
     */
    <T> InsertSpec<T> insertIgnore(Class<T> domainType, Object... args);
  
    interface InsertSpec<T>{
      /**
       * 保存单个对象
       */
      Homo<T> value(T obj);
      /**
       * 保存多个对象
       */
      Homo<List<T>> values(T... objs);
    }
  }
  ```
- SelectOperation 查询相关操作
```java
/**
* 查询操作
  */
  public interface SelectOperation {
  <T> SelectSpec<T> find(Class<T> domainType,Object ... args);

  interface SelectSpec<T>{
  Homo<Long> count();

       Homo<Boolean> exists();

       Homo<T> findFirst();

       // 查找一条，如果有多条，就报错
       Homo<T> findOne();

       Homo<List<T>> findAll();

       SelectSpec<T> matching(HomoQuery homoQuery);
  }
  }
```
- AggregateOperation 聚合相关操作
```java
/**
 * 数据聚合相关接口 实现groupBy, join等操作
 */
public interface AggregateOperation<P extends AggregateOperation.Aggregation> {
  <T> AggregateSpec<T> aggregate(P aggregation, Class<T> outputType, Object ... args);

  interface AggregateSpec<T>{
    Homo<T> first();

    Homo<List<T>> all();
  }

  interface Aggregation{
  }
}
public interface AggregationOp {
  OpType getOpType();

  enum OpType {
    PROJECT("project"),
    COUNT("count"),
    GROUP("group"),
    SKIP("skip"),
    LIMIT("limit"),
    SORT("sort"),
    MATCH("match"),
    UNWIND("unwind"),
    LOOKUP("lookup"),
    SUM("sum"),
    LAST("last"),
    FIRST("first"),
    AVG("avg"),
    MIN("min"),
    MAX("max"),
    ;

    private final String op;

    OpType(String op) {
      this.op = op;
    }
  }
}
```
- UpdateOperation 更新相关操作
```java
/**
 * 数据更新操作接口
 */
public interface UpdateOperation {
  <T> UpdateSpec<T> update(Class<T> type,Object ... args);

  interface UpdateSpec<T> {
    // 匹配条件
    UpdateSpec<T> matching(HomoQuery homoQuery);

    // 更新内容
    Homo<Long> apply(HomoUpdate homoUpdate);

    //对象更新，等同于save
    Homo<T> apply(T entity);
  }
}
```
- ExecuteOperation sql相关操作
```java

public interface ExecuteOperation {
  ExecuteSpec execute(String sql);

  interface ExecuteSpec{
    Homo<Map<String,Object>> one();

    Homo<Map<String,Object>> first();

    Homo<Integer> rowsUpdated();

    Homo<List<Map<String,Object>>> all();
  }
}
```
- DeleteOperation 删除相关操作
```java
/**
 * 数据删除操作接口
 */
public interface DeleteOperation {
  <T> DeleteSpec<T> delete(Class<T> type, Object ... args);

  interface DeleteSpec<T> {
    // 匹配条件
    DeleteSpec<T> matching(HomoQuery homoQuery);
    //return 删除的记录数
    Homo<Long> all();
  }
}
```
--- 

## 6. 定制化
- 可选配置
```java
public class RelationalProperties {
  /**
   * 类扫描路径
   */
  @Value("${homo.relational.base.package:com.homo}")
  private String basePackage;
}

```
<span style="font-size: 20px;">[返回主菜单](../../README.md)