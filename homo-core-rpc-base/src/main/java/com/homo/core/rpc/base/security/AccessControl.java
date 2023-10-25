package com.homo.core.rpc.base.security;

import com.homo.core.facade.security.RpcSecurity;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 接口调用权限控制器
 */
@Slf4j
public class AccessControl implements RpcSecurity {
    private AccessControl(Method method){

    }

    public static AccessControl create(Method method){
        return new AccessControl(method);
    }

    @Override
    public boolean isCallAllowed(String srcServiceName) {
        return true;
    }
}
