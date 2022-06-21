package com.homo.concurrent.schedule;


import com.homo.concurrent.event.TimerEvent;
import com.homo.concurrent.queue.CallQueue;
import lombok.Setter;

import java.util.function.Consumer;

public class HomoTimerTask extends AbstractHomoTimerTask{
    public TaskFun taskFun;
    public Object[] objects;
    public int runTimes;
    public int currentTimes;
    @Setter
    public volatile boolean interrupt;

    public HomoTimerTask(TaskFun taskFun, Object[] objects, int runTimes) {
        this.taskFun = taskFun;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(CallQueue callQueue, TaskFun taskFun, Object[] objects, int runTimes) {
        super(callQueue);
        this.taskFun = taskFun;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    public HomoTimerTask(CallQueue callQueue, Consumer<AbstractHomoTimerTask> onCancelConsumer, Consumer<AbstractHomoTimerTask> onErrorConsumer, TaskFun taskFun, Object[] objects, int runTimes) {
        super(callQueue, onCancelConsumer, onErrorConsumer);
        this.taskFun = taskFun;
        this.objects = objects;
        this.runTimes = runTimes;
    }

    @Override
    public void doRun() {
        currentTimes++;
        if (runTimes!=ENDLESS&&currentTimes>=runTimes){
            cancel();
        }
        addEvent(new TimerEvent(this,taskFun,objects,interrupt));
    }
}
