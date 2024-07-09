package com.homo.core.utils.trace;

import brave.Span;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class TraceLogUtil {
    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";

    public static void setTraceIdBySpan(Span span, String... from) {
        String traceId = "";
        String spanId = "";
        if (span != null) {
            traceId = span.context().traceIdString();
            spanId = span.context().spanIdString();
        }

        setTraceId(traceId, spanId);
        log.info("setTraceId span {} traceId {} spanId {} from {}", span, traceId, spanId, from);
    }

    public static void setTraceId(String traceId, String spanId) {
        MDC.put(TRACE_ID_KEY, traceId); // 将TraceId放入MDC中
        MDC.put(SPAN_ID_KEY, spanId);
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static void clearTrace() {
        MDC.remove(TRACE_ID_KEY); // 清理MDC中的TraceId
    }

}
