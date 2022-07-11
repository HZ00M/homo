package com.homo.concurrent.schedule;

import brave.Span;
import brave.Tracer;
import com.homo.concurrent.event.Event;
import com.homo.concurrent.queue.CallQueue;
import com.homo.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

;

@Log4j2
public abstract class AbstractHomoTimerTask implements Runnable {
    public static int ENDLESS = 0;
    private final CallQueue callQueue;
    public ScheduledFuture future;
    private final Consumer<AbstractHomoTimerTask> onErrorConsumer;
    public AbstractHomoTimerTask(){
        this(CallQueueMgr.getInstance().getLocalQueue());
    }
    public AbstractHomoTimerTask(CallQueue callQueue){
        this(callQueue,null);
    }
    public AbstractHomoTimerTask(CallQueue callQueue,  Consumer<AbstractHomoTimerTask> onErrorConsumer){
        this.callQueue = callQueue;
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
