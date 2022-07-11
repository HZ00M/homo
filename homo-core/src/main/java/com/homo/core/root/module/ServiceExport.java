package com.homo.core.root.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 暴露一个接口类为服务接口
 * note: 如果是有状态服务,此服务即为主服务,与存储绑定,不可更改!
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExport {
    String ServiceName() default ""; // 服务名字
    String DriverType() default "grpc"; // 服务类型
    boolean isStateful(); //是否是有状态服务
}