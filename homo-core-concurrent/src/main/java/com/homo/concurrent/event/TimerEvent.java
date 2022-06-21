package com.homo.concurrent.event;

import com.homo.concurrent.schedule.TaskFun;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

@Slf4j
public class TimerEvent extends AbstractBaseEvent{

    TimerTask timerTask;
    TaskFun taskFun;
    Object[] params;
    boolean interrupt;

    public TimerEvent(TimerTask timerTask, TaskFun taskFun, Object[] params,boolean interrupt) {
        this.timerTask = timerTask;
        this.taskFun = taskFun;
        this.params = params;
        this.interrupt = interrupt;
    }

    @Override
    public void process() {
        try {
            taskFun.run(params);
        }catch (Exception e){
            log.error("TimerEvent error",e);
            if (interrupt){
                timerTask.cancel();
            }
        }
    }
}
