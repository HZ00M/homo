# homo-core
##简介
针对游戏开发场景设计的全响应一站式分布式框架
```text
  homo-core是针对游戏开发场景设计的分布式框架，支持常用的http,grpc协议的远程调用，
  支持负载均衡和有状态调用。全项目使用响应式编程模型，封装了事件模型和接口，降低并发
  编程的学习使用成本。支持短连接，长连接网关，支持tcp及http网关，支持多级缓存及有状态
  调用,一键部署等。
```
## 框架介绍
- ### Rpc框架
  - [rpc简介](docs/Rpc/Rpc概要介绍.md)
  - [基于HTTP的rpc调用](docs/Rpc/基于Http的Rpc调用.md)
  - [基于Grpc的rpc调用](docs/Rpc/基于Grpc的Rpc调用.md)
- ### Entity
  - [有状态Entity存储服务](docs/Entity/有状态Entity服务.md)
- ### 消息队列
- ### 基础设施
  - [gate使用文档](docs/交互网关/网关设计使用文档.md)
  - [mongo驱动使用文档](docs/响应式Mongo/mongo存储设计文档.md)
  - [cache驱动使用文档](docs/缓存/缓存驱动设计使用文档.md)
  - [lock锁驱动使用文档](docs/分布式锁/分布式锁设计使用文档.md)
  - [链路追踪使用文档](docs/链路追踪/zipkin使用文档.md)
  - [storage使用文档](docs/存储/存储结构设计文档.md)