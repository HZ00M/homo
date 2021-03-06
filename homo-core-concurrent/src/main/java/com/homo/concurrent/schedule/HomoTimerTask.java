package com.homo.concurrent.schedule;


import com.homo.concurrent.event.TimerTaskEvent;
import com.homo.concurrent.queue.CallQueue;
import lombok.Setter;

import java.util.function.Consumer;

public class HomoTimerTask<T extends Task> extends AbstractHomoTimerTask {
    public T task;
    public Object[] objects;
    public int runTimes;
    public int currentTimes;
    @Setter
    public volatile boolean interrupt;

    public HomoTimerTask(T task, int runTimes, Object... objects) {
        this.task = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(CallQueue callQueue, T task, int runTimes, Object... objects) {
        super(callQueue);
        this.task = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(CallQueue callQueue,  Consumer<AbstractHomoTimerTask> onErrorConsumer, T task, int runTimes, Object... objects) {
        super(callQueue, onErrorConsumer);
        this.task = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    @Override
    public void doRun() {
        currentTimes++;
        if (runTimes != ENDLESS && currentTimes >= runTimes) {
            future.cancel(true);
        }
        addEvent(new TimerTaskEvent<T>(this, task, interrupt, objects));
    }
}
