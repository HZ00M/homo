package com.homo.core.facade.excption;

public enum HomoError {
    defaultError(0, "system error"),
    callAllow(100,"call fun not allow"),
    callEmpty(101,"call empty"),
    callError(102,"not support yet" ),
    rpcTimeOutException(103, "asyncBytesStreamCall time out"),

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
