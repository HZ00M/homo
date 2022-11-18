package com.homo.core.utils.exception;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum HomoError implements HomoThrowable {
    defaultError(0, "system error"),
    callAllow(100, "call fun not allow"),
    callEmpty(101, "call empty"),
    callError(102, "not support yet"),
    remoteError(103, "msgId %s error"),
    hostNotFound(104, "tagName  %s hostNotFound"),
    rpcTimeOutException(105, "asyncBytesStreamCall time out"),
    choicePodNotFound(106, "tagName %s choice pod not found"),
    httpMaxByteError(107, "bytesLength > bytesLimit size %s limit %s"),
    rpcAgentTypeNotSupport(108, "RpcAgent no support rpcType %s tagName %s"),
    gateError(109, "gateError %s"),
    broadcastError(110, "broadcastError");

    private int code;
    private String message;
    private static final Map<Integer, String> errorDefineMap = new ConcurrentHashMap<>();

    static {
        for (HomoError homoError : HomoError.values()) {
            HomoError.appendError(homoError.code, homoError.message);
        }
    }

    HomoError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static void appendError(Integer code, String message) {
        Assert.isNull(errorDefineMap.get(code), "code repeat");
        errorDefineMap.put(code, message);
    }

    public static HomoException throwError(HomoThrowable type, Object... args) {
        return new HomoException(type.getCode(), type.msgFormat(args));
    }

    public static HomoException throwError(int errorCode, Object... args) {
        String message = errorDefineMap.getOrDefault(errorCode, defaultError.message);
        return new HomoException(errorCode, HomoThrowable.msgFormat(message, args));
    }

    public String msgFormat(Object... args) {
        return String.format(this.message, args);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    public String getMessage() {
        return message;
    }
}
