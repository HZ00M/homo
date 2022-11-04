package com.homo.core.rpc.base.security;

import com.homo.core.facade.security.RpcSecurity;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;

/**
 * 接口调用权限控制器
 */
@Log4j2
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
