package com.homo.turntable.core.error;


public class FormatException extends RuntimeException {
    public FormatException(String message) {
        super(message);
    }

    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
