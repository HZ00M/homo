package com.homo.core.gate.tcp;

import java.util.Arrays;

public enum MessageType {
    UNKNOWN,
    PROTO,
    JSON,
    HEART_BEAT;

    public static MessageType getType(int origin) {
        return Arrays.stream(MessageType.values()).filter(item -> item.ordinal() == origin).findFirst().orElse(MessageType.UNKNOWN);
    }
}
