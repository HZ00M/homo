package com.homo.concurrent.event;

import brave.Span;

public abstract class AbstractBaseEvent implements BaseEvent{
    public Span span;
    public boolean processed;
    public String mark;

    @Override
    public boolean processed() {
        return processed;
    }

    @Override
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }


    @Override
    public Span getSpan() {
        return span;
    }

    @Override
    public void setSpan(Span span) {
        this.span = span;
    }

    @Override
    public String getTagForSpan() {
        return mark;
    }
}
