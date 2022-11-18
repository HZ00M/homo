package com.homo.core.utils.exception;

public interface HomoThrowable {
    int getCode();

    String message();

    default String msgFormat(Object... args) {
        return String.format(message(), args);
    }
    static String msgFormat(String message,Object... args) {
        return String.format(message, args);
    }
}
