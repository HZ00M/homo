package com.homo.core.tread.processor.exception;

public class SetOpException extends RuntimeException {
    public static String temp = "Resource setOp error method %s resource %s cause %s";
    private final String message;

    public SetOpException(Object target, String source, String cause) {
        message = String.format(temp, target, source, cause);
    }

    public SetOpException(Object target, String source, Throwable throwable) {
        super(throwable);
        message = String.format(temp, target, source, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
