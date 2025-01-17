package com.homo.core.rpc.http;

public enum HomoHttpHeader {
    X_TRACE_ID("X-TRACE-ID"),
    X_SPAN_ID("X-SPAN-ID"),
    X_SAMPLED("X-SAMPLED"),
    ;

    private final String param;

    HomoHttpHeader(String PARAM) {
        this.param = PARAM;
    }

    public String param() {
        return param;
    }
}
