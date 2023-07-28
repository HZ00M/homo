package com.homo.core.facade.service;

import com.homo.core.facade.rpc.RpcType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 暴露一个接口类为服务接口
 * http服务返回值只能是string
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExport {
    String tagName() default ""; //服务名（格式：serviceName:port）
    RpcType driverType() default RpcType.grpc;    //服务类型
    boolean isStateful() default true; //是否是有状态服务器
    boolean isMainServer(); //是否是主服务，一个进程可能有多个服务，主服务用来向外部发起调用
}


