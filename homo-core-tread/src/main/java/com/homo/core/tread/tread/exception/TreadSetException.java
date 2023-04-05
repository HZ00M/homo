package com.homo.core.tread.tread.exception;

public class TreadSetException extends Exception {
    public static String temp = "Tread setMethod error method name %s source %s opValue %s cause %s";
    private final String message;

    public TreadSetException(String methodName , String source, String opValue, Throwable throwable) {
        super(throwable);
        message = String.format(temp, methodName, source, opValue,throwable.getMessage());
    }

    @Override
    public String getMessage() {
        return message;
    }
}
