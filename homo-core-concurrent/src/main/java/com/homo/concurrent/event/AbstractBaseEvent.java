package com.homo.concurrent.event;


public abstract class AbstractBaseEvent implements BaseEvent{

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


}
