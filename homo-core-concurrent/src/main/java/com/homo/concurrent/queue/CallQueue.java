package com.homo.concurrent.queue;

import brave.Span;
import com.homo.concurrent.event.BaseEvent;
import com.homo.concurrent.event.Event;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


@Log4j2
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
        if (ZipkinUtil.getTracing() != null && e instanceof BaseEvent) {
            BaseEvent event = (BaseEvent) e;
            Span span = event.getTraceInfo() != null ? event.getTraceInfo() : ZipkinUtil.getTracing().tracer().currentSpan();
            event.setTraceInfo(span);
            event.annotate("add-event");
            event.getTraceInfo().tag("RunningEvent", event.getClass().getSimpleName());
            event.getTraceInfo().tag("waitingTaskNum",String.valueOf( waitingEventNum));
            eventQueue.add(event);
        }else {
            eventQueue.add(e);
        }
    }

    public void start(CallQueueMgr callQueueMgr){
        log.info("CallQueue[{}] start!",id);
        callQueueMgr.executorService.submit(()->{
            //线程运行时，将当前线程的queue设置到threadLocal上
            callQueueMgr.setLocalQueue(this);
            running = true;
            while (!isShutDown){
                try {
                    Event event = eventQueue.poll(1L, TimeUnit.SECONDS);
                    if (event==null){
                        continue;
                    }
                    event.doProcess();
                }catch (Exception e){
                    log.error("CallQueue[{}] run error cause:",id,e);
                }finally {
                    log.debug("CallQueue[{}] run finish",id);
                }
            }
            log.info("CallQueue[{}] shutdown",id);
            running = false;
        });
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
