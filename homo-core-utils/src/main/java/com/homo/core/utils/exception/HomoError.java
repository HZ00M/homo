package com.homo.core.utils.exception;

import java.util.Arrays;

public enum HomoError {
    defaultError(0, "system error"),
    callAllow(100,"call fun not allow"),
    callEmpty(101,"call empty"),
    callError(102,"not support yet" ),
    remoteError(103,"msgId %s error" ),
    hostNotFound(104,"tagName  %s hostNotFound" ),
    rpcTimeOutException(105, "asyncBytesStreamCall time out"),
    choicePodNotFound(106,"tagName %s choice pod not found" ),
    httpMaxByteError(107, "bytesLength > bytesLimit size %s limit %s"),
    rpcAgentTypeNotSupport(108, "RpcAgent no support rpcType %s tagName %s"),
    gateError(109, "gateError %s"),
    broadcastError(110,"broadcastError")
    ;

    private int code;
    private String message;

    HomoError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static HomoException throwError(HomoError type, Object... args) {
        return new HomoException(type.getCode(), type.msgFormat(args));
    }

    public static HomoException throwError(int errorCode, Object... args) {
        HomoError homoError = Arrays.stream(values()).filter(item -> item.code == errorCode).findFirst().orElse(HomoError.defaultError);
        return new HomoException(homoError.code, homoError.msgFormat(args));
    }

    public String msgFormat(Object... args) {
        return String.format(this.message, args);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
