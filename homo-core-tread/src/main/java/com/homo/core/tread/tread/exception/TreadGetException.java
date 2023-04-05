package com.homo.core.tread.tread.exception;

public class TreadGetException extends Exception {
    public static String temp = "Tread getMethod error method %s source %s cause %s";
    private final String message;

    public TreadGetException(String methodName,String source, String cause) {
        message = String.format(temp, methodName,source, cause);
    }

    public TreadGetException(String methodName,String source, Throwable throwable) {
        super(throwable);
        message = String.format(temp, methodName,source, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
