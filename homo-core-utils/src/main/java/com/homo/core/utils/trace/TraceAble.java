package com.homo.core.utils.trace;

public interface TraceAble<TRACE_INFO> {
    void setTraceInfo(TRACE_INFO traceInfo);

    TRACE_INFO getTraceInfo();
}
