package com.homo.concurrent.event;

import brave.Span;
import brave.Tracer;
import com.homo.core.utils.trace.TraceAble;
import com.homo.core.utils.trace.ZipkinUtil;

/**
 * 支持链路追踪的异步事件处理
 */
public interface BaseEvent extends Event,TraceAble<Span> {
    default void doProcess() {
        long processTime = System.currentTimeMillis();
        Span span = getTraceInfo();
        if (span != null) {
            span.annotate("process-event");
            try (Tracer.SpanInScope ss = ZipkinUtil.getTracing().tracer().withSpanInScope(span)) {
                span.tag("thread",Thread.currentThread().getName());
                preProcess();
                process();
            }catch (Throwable throwable){
                span.error(throwable);
            }finally {
                long spentTime = System.currentTimeMillis() - processTime;
                if (spentTime > 1000){
                    log.warn("{} process too long! spentTime_{}", getName(),spentTime);
                }
                span.annotate("process-event-end");
                span.tag("process-spend-time", String.valueOf(spentTime));
                afterProcess();
            }
        }else {
            doProcess();
        }
    }

    default Span annotate(String annotate) {
        if (getTraceInfo() == null) {
            return null;
        }
        return getTraceInfo().annotate(annotate);
    }

    default Span name(String name) {
        if (getTraceInfo() == null) {
            return null;
        }
        return getTraceInfo().name(name);
    }

//    default Span spanTag(String key, String value) {
//        if (getSpan() == null) {
//            return null;
//        }
//        return getSpan().tag(key, value);
//    }
//
//    default Span getSpan(){
//        throw new RuntimeException("event not override method: cancel");
//    }
//
//    default void setSpan(Span span) {
//        throw new RuntimeException("event not override method: cancel");
//    }
//
//    default String getTagForSpan(){
//        return toString();
//    }

    default String getName(){
        return toString();
    }


}
