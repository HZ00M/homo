
# zipkin工具使用说明文档
## 简介
```text
    homo框架链路追踪支持
```

## 目的
- 提供链路追踪功能，框架所有模块都加入了链路追踪功能，进行简单配置即可使用链路追踪能力
## 前提
- 使用homo-core的框架
## 版本
- homo-core >= 1.0
###链路追踪配置
```java
@Configurable
@Data
public class ZipKinProperties {
    @Value(value = "${homo.zipkin.namespace:homo_zipkin_config}")
    public String zipikinNamespace ;
    /**
     * 链路追踪上报地址
     */
    @Value(value = "${homo.zipkin.server.addr:127.0.0.1}")
    public String reportAddr ;
    /**
     * 上报端口
     */
    @Value(value = "${homo.zipkin.server.port:9411}")
    public String reportPort ;
    /**
     * True表示跟踪系统支持在span.Kind之间共享span ID
     */
    @Value(value = "${homo.zipkin.server.supportsJoin:true}")
    public boolean supportsJoin ;
    /**
     * 每秒采样数
     */
    @Value(value = "${homo.zipkin.client.trace.perSecond:10}")
    public int tracesPerSecond;
    /**
     * 链路追踪开关
     */
    @Value(value = "${homo.zipkin.client.trace.open:false}")
    public boolean isOpen  = false;
}
```

<span style="font-size: 20px;">[返回主菜单](../../README.md)
 