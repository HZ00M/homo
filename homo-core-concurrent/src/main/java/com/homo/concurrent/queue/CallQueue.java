package com.homo.concurrent.queue;

import brave.Span;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;

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

    public CallQueue(int id, int queueSize) {
        this.id = id;
        this.queueMaxSize = queueSize;
        eventQueue = new LinkedBlockingDeque<>(queueSize);
    }

    /**
     * 将事件交给线程池处理 支持链路追踪功能
     * @param e
     */
    public void addEvent(Event e) {
        final int waitingEventNum = getWaitingTasksNum();
        if (getWaitingTasksNum() >= queueMaxSize) {
            log.error("CallQueue id_{} addEvent error , to much waiting event waitingEventNum_{} event_{}", id, waitingEventNum, e, new Exception("to much waiting Event"));
        }
        if (ZipkinUtil.getTracing() != null && e instanceof TraceableEvent) {
            TraceableEvent event = (TraceableEvent) e;
            Span span = event.getSpan() != null ? event.getSpan() : ZipkinUtil.getTracing().tracer().currentSpan();
            event.setSpan(span);
            event.annotate("add-event");
            event.spanTag("RunningEvent", event.getClass().getSimpleName());
            event.spanTag("waitingTaskNum",String.valueOf( waitingEventNum));
            eventQueue.add(event);
        }else {
            eventQueue.add(e);
        }
    }

    public void start(CallQueueMgr callQueueMgr){

    }

    public void shutdown(){
        isShutDown = true;
    }

    public boolean isRunning(){
        return running;
    }

    int getWaitingTasksNum(){
        return eventQueue.size();
    }
}
