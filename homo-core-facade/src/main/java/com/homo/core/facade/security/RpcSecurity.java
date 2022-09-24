package com.homo.core.facade.security;

public interface RpcSecurity {

    boolean isCallAllowed(String srcServiceName);
}
