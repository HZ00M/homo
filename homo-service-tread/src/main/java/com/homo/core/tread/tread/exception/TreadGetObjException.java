package com.homo.core.tread.tread.exception;

public class TreadGetObjException extends Exception {
    public static String temp = "Tread getObjMethod error target %s source %s cause %s";
    private final String message;

    public TreadGetObjException(Object target, String source, String cause) {
        message = String.format(temp, target, source, cause);
    }

    public TreadGetObjException(Object target, String source, Throwable throwable) {
        super(throwable);
        message = String.format(temp, target, source, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
