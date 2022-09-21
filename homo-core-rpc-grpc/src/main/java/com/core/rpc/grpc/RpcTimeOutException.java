package com.core.rpc.grpc;

public class RpcTimeOutException extends RuntimeException{
    public RpcTimeOutException(String message){
        super(message);
    }
}

