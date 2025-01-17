package com.homo.core.facade.rpc;

/**
 * rpc类型  目前支持http和grpc
 */
public enum RpcType {
    http,
    grpc;
    public static RpcType of(int original) {
        return values()[original];
    }
}
