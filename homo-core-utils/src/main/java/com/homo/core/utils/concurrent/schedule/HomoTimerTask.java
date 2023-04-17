package com.homo.core.utils.concurrent.schedule;


import com.homo.core.utils.concurrent.event.TimerTaskEvent;
import com.homo.core.utils.concurrent.queue.CallQueue;
import lombok.Setter;

import java.util.function.Consumer;

public class HomoTimerTask extends AbstractHomoTimerTask<HomoTimerTask> {
    public Runnable task;

    public Object[] objects;
    public int runTimes;//0表示无限次
    public int currentTimes;
    @Setter
    public volatile boolean interrupt;
    public HomoTimerTask(){

    }
    public HomoTimerTask(Runnable task, int runTimes, Object... objects) {
        this.task = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(CallQueue callQueue, Runnable task, int runTimes, Object... objects) {
        super(callQueue);
        this.task = task;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(CallQueue callQueue, Consumer<AbstractHomoTimerTask> onErrorConsumer, Runnable task, int runTimes, Object... objects) {
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
        //当开始执行定时任务时，将任务包装成事件塞入指定队列
        addEvent(new TimerTaskEvent(this, task, interrupt, objects));
    }
}
