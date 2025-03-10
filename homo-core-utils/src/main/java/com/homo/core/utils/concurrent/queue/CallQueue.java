package com.homo.core.utils.concurrent.queue;

import brave.Span;
import com.homo.core.utils.concurrent.event.Event;
import com.homo.core.utils.concurrent.event.TraceEvent;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


@Slf4j
public class CallQueue {
    @Getter
    int id;
    @Getter
    int queueMaxSize = 10000;
    volatile boolean running = false;
    volatile boolean isShutDown = false;
    LinkedBlockingDeque<Event> eventQueue;

    public CallQueue(int id) {
        this.id = id;
        eventQueue = new LinkedBlockingDeque<>();
    }

    public String name(){
        return Thread.currentThread().getName();
    }

    public CallQueue(int id, int queueSize) {
        this.id = id;
        this.queueMaxSize = queueSize;
        eventQueue = new LinkedBlockingDeque<>(queueSize);
    }

    /**
     * 将事件交给线程池处理 支持链路追踪功能
     *
     * @param e
     */
    public void addEvent(Event e) {
        final int waitingEventNum = getWaitingTasksNum();

        if (log.isTraceEnabled()) {
            log.trace("CallQueue addEvent queueId {} eventId {} waitingEventNum {} event {}", id, e.id(), waitingEventNum, e);
        }
        if (getWaitingTasksNum() >= queueMaxSize) {
            log.error("CallQueue queueId {} eventId {} addEvent error , to much waiting event waitingEventNum {} event {}", id, e.id(), waitingEventNum, e, new Exception("to much waiting Event"));
        }
        if (ZipkinUtil.getTracing() != null && e instanceof TraceEvent) {
            TraceEvent event = (TraceEvent) e;
            Span span = event.getSpan() != null ? event.getSpan() : ZipkinUtil.getTracing().tracer().currentSpan();
            event.setSpan(span);
            event.annotate("add-event");
            span.tag("RunningEvent", event.getClass().getSimpleName());
            span.tag("waitingTaskNum", String.valueOf(waitingEventNum));
            eventQueue.add(event);
        } else {
            eventQueue.add(e);
        }
    }

    public void start(CallQueueMgr callQueueMgr) {
        log.info("CallQueue[{}] start!", id);
        callQueueMgr.executorService.submit(() -> {
            //线程运行时，将当前线程的queue设置到threadLocal上
            callQueueMgr.setLocalQueue(this);
            running = true;
            while (!isShutDown) {
                try {
                    Event event = eventQueue.poll(1L, TimeUnit.SECONDS);
                    if (event == null) {
                        continue;
                    }
                    event.doProcess();
                } catch (Exception e) {
                    log.error("CallQueue[{}] run error cause:", id, e);
                } finally {
                    log.debug("CallQueue[{}] run finish", id);
                }
            }
            log.info("CallQueue[{}] shutdown", id);
            running = false;
        });
    }

    public void shutdown() {
        isShutDown = true;
    }

    public boolean isRunning() {
        return running;
    }

    int getWaitingTasksNum() {
        return eventQueue.size();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null){
            return false;
        }
        if (obj instanceof CallQueue) {
            return this.getId() == (((CallQueue) obj).getId());
        } else {
            return false;
        }

    }
}
