package com.homo.core.utils.concurrent.schedule;

import brave.Span;
import brave.Tracer;
import com.homo.core.utils.concurrent.event.TraceEvent;
import com.homo.core.utils.concurrent.event.Event;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

;

@Slf4j
public abstract class AbstractHomoTimerTask<T extends AbstractHomoTimerTask> extends TimerTask {
    @Setter
    public String id;

    public Runnable runnable;

    Consumer<AbstractHomoTimerTask> onCancelConsumer;
    public static int ENDLESS = 0;
    public CallQueue callQueue;
    public ScheduledFuture future;
    public boolean isCancel;
    Consumer<AbstractHomoTimerTask> onErrorConsumer;

    public AbstractHomoTimerTask() {
        this(CallQueueMgr.getInstance().getLocalQueue());
    }

    public AbstractHomoTimerTask(CallQueue callQueue) {
        this(callQueue, null);
    }

    public AbstractHomoTimerTask(CallQueue callQueue, Consumer<AbstractHomoTimerTask> onErrorConsumer) {
        this(callQueue, onErrorConsumer, null);
    }

    public AbstractHomoTimerTask(CallQueue callQueue, Consumer<AbstractHomoTimerTask> onErrorConsumer, Consumer<AbstractHomoTimerTask> onCancelConsumer) {
        Assert.notNull(callQueue, "callQueue can not be null");
        this.callQueue = callQueue;
        this.onErrorConsumer = onErrorConsumer;
        this.onCancelConsumer = onCancelConsumer;
    }

    public T setOnCancelConsumer(Consumer<AbstractHomoTimerTask> onCancelConzsumer) {
        this.onCancelConsumer = onCancelConsumer;
        return (T) this;
    }

    public T setOnErrorConsumer(Consumer<AbstractHomoTimerTask> onErrorConsumer) {
        this.onErrorConsumer = onErrorConsumer;
        return (T) this;
    }

    public T setCallQueue(CallQueue callQueue) {
        this.callQueue = callQueue;
        return (T) this;
    }

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            log.error("HomoTimerTask id {} error", id,e);
            if (onErrorConsumer != null) {
                onErrorConsumer.accept(this);
            }
        }
    }

    public boolean justCancel() {
        return super.cancel();
    }

    /**
     * 取消定时任务
     */
    @Override
    public boolean cancel() {
        isCancel = true;
        boolean rel = super.cancel();
        future.cancel(true);
        if (onCancelConsumer != null) {
            onCancelConsumer.accept(this);
        }
        return rel;
    }


    public abstract void doRun();

    protected void addEvent(Event event) {
        Span span = ZipkinUtil.getTracing().tracer()
                .nextSpanWithParent(arg -> false, "timer", null)
                .name("timer")
                .annotate("add-event")
                .tag("timer", this.toString())
                .tag("event", event.id());
        try (Tracer.SpanInScope scope = ZipkinUtil.getTracing().tracer().withSpanInScope(span)) {
            if (event instanceof TraceEvent) {
                TraceEvent traceEvent = (TraceEvent) event;
                traceEvent.setSpan(span);
            }
            callQueue.addEvent(event);
        } catch (Exception e) {
            log.error("addEvent error event {} e", event.id(), e);
            span.error(e);
        } finally {
            span.tag(ZipkinUtil.FINISH_TAG, "HomoTimerTask").finish(System.currentTimeMillis());
        }
    }

}
