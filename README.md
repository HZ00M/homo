# homo-core
##简介
```text
  homo-core是针对游戏开发场景设计的分布式框架，支持常用的http,grpc协议的远程调用，
  支持负载均衡和有状态调用。全项目使用响应式编程模型，封装了事件模型和接口，降低并发
  编程的学习使用成本。支持短连接，长连接网关，支持tcp及http网关，支持多级缓存及有状态
  调用,一键部署等。
```
## 框架介绍
- 基础设施 
    - [storage使用文档](docs/storage-doc/存储结构设计文档.md)
    - [rpc使用文档](docs/rpc-doc/Rpc调用文档.md)
    - [gate使用文档](docs/gate-doc/网关设计使用文档.md)
    - [mongo驱动使用文档](docs/mongo-doc/mongo存储设计文档.md)
    - [cache驱动使用文档](docs/cache-doc/缓存驱动设计使用文档.md)
    - [lock锁驱动使用文档](docs/lock-doc/分布式锁设计使用文档.md)
    - [链路追踪使用文档](docs/trace-doc/zipkin使用文档.md)