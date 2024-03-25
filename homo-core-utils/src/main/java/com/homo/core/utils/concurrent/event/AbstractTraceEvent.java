package com.homo.core.utils.concurrent.event;


import brave.Span;

public abstract class AbstractTraceEvent implements TraceEvent {
    protected Span span;
    public boolean processed;

    public String id;
    @Override
    public String id(){
        return id;
    }
    @Override
    public boolean processed() {
        return processed;
    }

    @Override
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    @Override
    public void setSpan(Span span) {
        this.span = span;
    }

    @Override
    public Span getSpan() {
        return span;
    }

}
