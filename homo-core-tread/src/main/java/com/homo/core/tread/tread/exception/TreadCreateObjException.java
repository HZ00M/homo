package com.homo.core.tread.tread.exception;

public class TreadCreateObjException extends Exception {
    public static String temp = "Tread createObjMethod error method %s source %s cause %s";
    private final String message;

    public TreadCreateObjException(Object target, String source, String cause) {
        message = String.format(temp, target, source, cause);
    }

    public TreadCreateObjException(Object target, String source, Throwable throwable) {
        super(throwable);
        message = String.format(temp, target, source, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
