package com.core.rpc.grpc.exception;

public class RpcTimeOutException extends RuntimeException{
    public RpcTimeOutException(String message){
        super(message);
    }
}

