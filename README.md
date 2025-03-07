# homo-core
## 简介

### java游戏服务器框架，针对游戏开发场景设计的全响应一站式分布式框架
```text
  homo-core是针对游戏开发场景设计的分布式框架，支持常用的http,grpc协议的远程调用，
  支持负载均衡和有状态调用。全项目使用响应式编程模型，封装了事件模型和接口，降低并发
  编程的学习使用成本。支持短连接，长连接网关，支持tcp及http网关，支持多级缓存及有状态
  调用,一键部署等。
```
## 框架介绍
- ### Rpc【远程调用能力】
  - [Rpc简介](docs/Rpc/Rpc概要介绍.md)
  - [Rpc-HTTP](docs/Rpc/基于Http的Rpc调用.md)
  - [Rpc-GRPC](docs/Rpc/基于Grpc的Rpc调用.md)
- ### Storage 【自动化存储】
  - [Storage简介](docs/Storage/Storage概要介绍.md)
  - [Landing落地规则](docs/Storage/Landing规则文档.md)
  - [基于Redis+Mysql的自动落地功能](docs/Storage/Redis_Mysql_Storage.md)
- ### Entity 【领域驱动、有状态、远程调用、自存储对象】
  - [有状态Entity存储服务](docs/Entity/有状态Entity服务.md)
- ### MessageQueue 【生产&发布者】
  - [消息队列简介](docs/消息队列/基于Kafka的消息队列.md)
  - [MQ-Kafka驱动](docs/消息队列/基于Kafka的消息队列.md)
  - [MQ-Redis驱动](docs/待定.md)
- ### Relational 【响应式的CRUD】
  - [响应式数据库简介](docs/响应式数据库/响应式数据库概要介绍.md)
  - [Relational-Mysql驱动](docs/响应式数据库/Mysql响应式数据库驱动.md)
  - [Relational-Mongo驱动](docs/待定.md)
- ### Document 【文档型数据库支持】
  - [Document简介](docs/Document/Document概要介绍.md)
  - [Document-Mongo驱动](docs/Document/Mongo驱动.md) 
- ### Homo-Plugin 【Maven插件】
  - [一键部署K8S工具](docs/插件/一键部署插件.md)
  - [一键转表工具](docs/插件/一键导表插件.md)
- ### Gate  【网关】
  - [Gate-Tcp驱动](docs/交互网关/网关设计使用文档.md)  
- ### Cache 【缓存】
  - [Cache-Redis驱动](docs/缓存/缓存驱动设计使用文档.md) 
- ### Lock 【分布式锁】
  - [Lock-Redis锁驱动](docs/分布式锁/分布式锁设计使用文档.md) 
- ### Trace 【链路追踪】
  - [Trace-Zipkin](docs/链路追踪/zipkin使用文档.md) 

## 案例工程
- homo-game【github】： [github.com/HZ00M/homo-game](https://gitee.com/Hzoom/homo-game)
- homo-game【gitee】： [gitee.com/Hzoom/homo-game](https://gitee.com/Hzoom/homo-game)