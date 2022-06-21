package com.homo.concurrent.schedule;

import brave.Span;
import brave.Tracer;
import com.homo.concurrent.queue.CallQueue;
import com.homo.concurrent.queue.CallQueueMgr;
import com.homo.concurrent.event.Event;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractHomoTimerTask extends TimerTask {
    public static int ENDLESS = 0;
    private final CallQueue callQueue;
    private final Consumer<AbstractHomoTimerTask> onCancelConsumer;
    private final Consumer<AbstractHomoTimerTask> onErrorConsumer;
    public AbstractHomoTimerTask(){
        this(CallQueueMgr.getInstance().getLocalQueue());
    }
    public AbstractHomoTimerTask(CallQueue callQueue){
        this(callQueue,null,null);
    }
    public AbstractHomoTimerTask(CallQueue callQueue, Consumer<AbstractHomoTimerTask> onCancelConsumer, Consumer<AbstractHomoTimerTask> onErrorConsumer){
        this.callQueue = callQueue;
        this.onCancelConsumer = onCancelConsumer;
        this.onErrorConsumer = onErrorConsumer;
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

    @Override
    public boolean cancel(){
        boolean result = super.cancel();
        if (onCancelConsumer!=null){
            onCancelConsumer.accept(this);
        }
        return result;
    }

    public abstract void doRun();

    protected void addEvent(Event event){
        Span span = ZipkinUtil.getTracing().tracer().nextSpan().name("timer").tag("CallQueue", String.valueOf(callQueue.getId()));
        try(Tracer.SpanInScope scope = ZipkinUtil.getTracing().tracer().withSpanInScope(span)){
            callQueue.addEvent(event);
        }catch (Exception e){
            span.error(e);
        }finally {
            span.tag(ZipkinUtil.FINISH_TAG,"HomoTimerTask").finish(System.currentTimeMillis());
        }
    }

}
