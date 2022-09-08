package com.homo.core.facade.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 暴露一个接口类为服务接口
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExport {
    String ServiceName() default ""; //服务名
    DriverType DriverType() default DriverType.grpc;    //服务类型
    boolean isStateful() default false; //是否是有状态服务器
    enum DriverType{
        http,grpc
    }
}


