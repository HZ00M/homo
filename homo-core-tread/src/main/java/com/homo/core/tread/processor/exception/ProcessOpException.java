package com.homo.core.tread.processor.exception;

public class ProcessOpException extends RuntimeException {
    public static String temp = " Process exec error cause %s";
    private final String message;

    public ProcessOpException(String cause) {
        message = String.format(temp,  cause);
    }

    public ProcessOpException(Throwable throwable) {
        super(throwable);
        message = String.format(temp, throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
