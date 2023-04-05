package com.homo.core.tread.processor.exception;

public class CheckOpException extends RuntimeException {
    public static String temp = "Resource checkOp error method %s resource %s cause %s";
    private final String message;

    public CheckOpException(String methodName, String source, String cause) {
        message = String.format(temp, methodName,source, cause);
    }

    public CheckOpException(String methodName, String source, Throwable throwable) {
        super(throwable);
        message = String.format(temp, methodName,source, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
