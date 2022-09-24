package com.homo.core.rpc.base.security;

import com.homo.core.facade.security.RpcSecurity;

import java.lang.reflect.Method;

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
