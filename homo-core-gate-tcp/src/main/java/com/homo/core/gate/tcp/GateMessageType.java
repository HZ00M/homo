package com.homo.core.gate.tcp;

import java.util.Arrays;

public enum GateMessageType {
    UNKNOWN,
    PROTO,
    JSON,
    HEART_BEAT;

    public static GateMessageType getType(int origin) {
        return Arrays.stream(GateMessageType.values()).filter(item -> item.ordinal() == origin).findFirst().orElse(GateMessageType.UNKNOWN);
    }
}
