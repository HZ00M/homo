package com.homo.core.utils.concurrent.event;

import brave.Span;
import brave.Tracer;
import com.homo.core.utils.trace.ZipkinUtil;

/**
 * 支持链路追踪的异步事件处理
 */
public interface TraceEvent extends Event {

    default void doProcess() {
        long processTime = System.currentTimeMillis();
        Span span = getSpan();
        if (span != null ) {
            if (traceEnable()){
                span.annotate("process-event");
            }
            try (Tracer.SpanInScope ss = ZipkinUtil.getTracing().tracer().withSpanInScope(span)) {
                if (traceEnable()){
                    span.tag("thread",Thread.currentThread().getName());
                }
                preProcess();
                process();
            }catch (Throwable throwable){
                span.error(throwable);
            }finally {
                if (traceEnable()){
                    long spentTime = System.currentTimeMillis() - processTime;
                    if (spentTime > 1000){
                        log.warn("{} process too long! spentTime {}", getName(),spentTime);
                    }
                    span.annotate("finish-event");
                    span.tag("process-spend-time", String.valueOf(spentTime));
                }
                afterProcess();
            }
        }else {
            try{
                preProcess();
                process();
            }catch (Throwable throwable) {
                log.error("event process error", throwable);
            }finally {
                afterProcess();
            }
        }
    }

    default Span getSpan(){
        return null;
    }

    default void setSpan(Span span){

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
