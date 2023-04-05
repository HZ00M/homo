package com.homo.core.tread.tread.exception;

public class TreadSetObjException extends Exception {
    public static String temp = "Tread setObjMethod error method %s source %s cause %s";
    private final String message;

    public TreadSetObjException(Object target, String source, String cause) {
        message = String.format(temp, target, source, cause);
    }

    public TreadSetObjException(Object target, String source, Throwable throwable) {
        super(throwable);
        message = String.format(temp, target, source, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
