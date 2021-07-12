package com.homo.core.utils.exception;

public class LockFailException extends Exception {
    public LockFailException() {
        super();
    }

    public LockFailException(String message) {
        super(message);
    }

    public LockFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockFailException(Throwable cause) {
        super(cause);
    }
}
