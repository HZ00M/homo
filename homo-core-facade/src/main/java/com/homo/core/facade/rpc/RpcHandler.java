package com.homo.core.facade.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rpc调用处理接口
 * 接口上加该注解将该接口声明成代理bean
 * 实际由RpcProxy发起rpc请求
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcHandler {
}
