package com.homo.turntable.core.error;

public class KeyRepeatedException extends RuntimeException {
    public KeyRepeatedException(String key) {
        super(key);
    }
}
