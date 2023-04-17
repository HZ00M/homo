package com.homo.core.utils.concurrent.schedule;

import brave.Span;
import brave.Tracer;
import com.homo.core.utils.concurrent.event.BaseEvent;
import com.homo.core.utils.concurrent.event.Event;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;

import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

;

@Log4j2
public abstract class AbstractHomoTimerTask<T extends AbstractHomoTimerTask> extends TimerTask {
    Consumer<AbstractHomoTimerTask> onCancelConsumer;
    public static int ENDLESS = 0;
    public  CallQueue callQueue;
    public ScheduledFuture future;
    Consumer<AbstractHomoTimerTask> onErrorConsumer;
    public AbstractHomoTimerTask(){
        this(CallQueueMgr.getInstance().getLocalQueue());
    }
    public AbstractHomoTimerTask(CallQueue callQueue){
        this(callQueue,null);
    }
    public AbstractHomoTimerTask(CallQueue callQueue,  Consumer<AbstractHomoTimerTask> onErrorConsumer){
        this(callQueue,onErrorConsumer,null);
    }

    public AbstractHomoTimerTask(CallQueue callQueue,  Consumer<AbstractHomoTimerTask> onErrorConsumer,Consumer<AbstractHomoTimerTask> onCancelConsumer){
        this.callQueue = callQueue;
        this.onErrorConsumer = onErrorConsumer;
        this.onCancelConsumer = onCancelConsumer;
    }

    public T  setOnCancelConsumer(Consumer<AbstractHomoTimerTask> onCancelConsumer) {
        this.onCancelConsumer = onCancelConsumer;
        return (T) this;
    }

    public T setOnErrorConsumer(Consumer<AbstractHomoTimerTask> onErrorConsumer) {
        this.onErrorConsumer = onErrorConsumer;
        return (T) this;
    }

    public T setCallQueue(CallQueue callQueue){
        this.callQueue = callQueue;
        return (T) this;
    }

    @Override
    public void run() {
        try {
            doRun();
        }catch (Exception e){
            log.error("HomoTimerTask error",e);
            if (onErrorConsumer!=null){
                onErrorConsumer.accept(this);
            }
        }
    }

    public boolean justCancel(){
        return super.cancel();
    }

    /**
     * 取消定时任务
     */
    @Override
    public boolean cancel() {
        boolean rel = super.cancel();
        if (onCancelConsumer != null) {
            onCancelConsumer.accept(this);
        }
        return rel;
    }


    public abstract void doRun();

    protected void addEvent(Event event){

        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("timer").tag("CallQueue", String.valueOf(callQueue.getId()));
        try(Tracer.SpanInScope scope = ZipkinUtil.getTracing().tracer().withSpanInScope(span)){
            if (event instanceof BaseEvent){
                BaseEvent baseEvent = (BaseEvent) event;
                baseEvent.setSpan(span);
            }
            callQueue.addEvent(event);
        }catch (Exception e){
            span.error(e);
        }finally {
            span.tag(ZipkinUtil.FINISH_TAG,"HomoTimerTask").finish(System.currentTimeMillis());
        }
    }

}
