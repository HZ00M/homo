package com.homo.concurrent.queue;

import brave.Span;

/**
 * 支持链路追踪的异步事件处理
 */
public interface TraceableEvent extends Event {
    default void doProcess() {
        long processTime = System.currentTimeMillis();
        Span span = getSpan();
        if (span!=null){
            span.annotate("process-event");

        }
    }

    default Span annotate(String annotate) {
        if (getSpan() == null) {
            return null;
        }
        return getSpan().annotate(annotate);
    }

    default Span name(String name) {
        if (getSpan() == null) {
            return null;
        }
        return getSpan().name(name);
    }

    default Span spanTag(String key, String value) {
        if (getSpan() == null) {
            return null;
        }
        return getSpan().tag(key, value);
    }

    public Span getSpan();

    public void setSpan(Span span);
}
