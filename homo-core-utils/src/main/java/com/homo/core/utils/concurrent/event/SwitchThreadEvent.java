package com.homo.core.utils.concurrent.event;

import brave.Span;
import com.homo.core.utils.rector.HomoSink;
import com.homo.core.utils.trace.TraceLogUtil;

public class SwitchThreadEvent extends AbstractTraceEvent {
    private HomoSink sink;
    private String form;
    private String target;
    private Object ret;

    public SwitchThreadEvent(String form, String target, HomoSink sink, Object ret, Span span) {
        this.form = form;
        this.target = target;
        this.sink = sink;
        this.span = span;
        this.ret = ret;
    }

    @Override
    public boolean traceEnable(){
        return false;
    }

    @Override
    public void process() {
        TraceLogUtil.setTraceIdBySpan(span, "process");
        log.info("switchThread process form {} target {}", form, target);
        sink.success(ret);
    }
}
