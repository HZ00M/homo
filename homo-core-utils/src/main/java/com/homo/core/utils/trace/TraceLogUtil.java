package com.homo.core.utils.trace;

import brave.Span;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
public class TraceLogUtil {
    public static final String TRACE_ID_KEY = "traceId";
    public static void setTraceIdBySpan(Span span,String... from) {
        String traceId;
        if (span != null){
            traceId = span.context().traceIdString();
        }else {
            traceId = UUID.randomUUID().toString();
        }
        setTraceId(traceId,from);
        log.info("setTraceId span {} traceId {} from {}", span,traceId,from);
    }
    public static void setTraceId(String traceId,String... from) {
        MDC.put(TRACE_ID_KEY, traceId); // 将TraceId放入MDC中
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static void clearTrace() {
        MDC.remove(TRACE_ID_KEY); // 清理MDC中的TraceId
    }

}
