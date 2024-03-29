package com.homo.core.utils.concurrent.schedule;


import com.homo.core.utils.concurrent.event.TimerTaskEvent;
import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.queue.CallQueueMgr;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class HomoTimerTask extends AbstractHomoTimerTask<HomoTimerTask> {

    public Object[] objects;
    public int runTimes;//0表示无限次
    public int currentTimes;
    @Setter
    public volatile boolean interrupt;

    public HomoTimerTask(String id, Runnable task) {
        super(CallQueueMgr.getInstance().getLocalQueue());
        this.id = id;
        this.runnable = task;
    }

    public HomoTimerTask(String id, Runnable task, int runTimes, Object... objects) {
        super(CallQueueMgr.getInstance().getLocalQueue());
        this.id = id;
        this.runnable = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(String id, CallQueue callQueue, Runnable task, int runTimes, Object... objects) {
        super(callQueue);
        this.id = id;
        this.runnable = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(String id, CallQueue callQueue, Consumer<AbstractHomoTimerTask> onErrorConsumer, Runnable task, int runTimes, Object... objects) {
        super(callQueue, onErrorConsumer);
        this.id = id;
        this.runnable = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    @Override
    public void doRun() {
        log.trace("HomoTimerTask doRun id {}  currentTimes {} runTimes {} isCancel {}", id, currentTimes, runTimes, isCancel);
        if (runTimes != ENDLESS && currentTimes >= runTimes) {
            log.trace("HomoTimerTask future cancel id {}  currentTimes {} runTimes {} isCancel {}", id, currentTimes, runTimes, isCancel);
            future.cancel(true);
            return;
        }
        //当开始执行定时任务时，将任务包装成事件塞入指定队列
        addEvent(new TimerTaskEvent(this, interrupt, objects));
        currentTimes++;

    }
}
