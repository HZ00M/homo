package com.homo.core.tread.processor.exception;

public class GetOpException extends RuntimeException {
    public static String temp = "Resource getOp error method %s resource %s cause %s";
    private final String message;

    public GetOpException(String methodName, String source, String cause) {
        message = String.format(temp, methodName,source, cause);
    }

    public GetOpException(String methodName, String source, Throwable throwable) {
        super(throwable);
        message = String.format(temp, methodName,source, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
