package com.homo.concurrent.event;

import com.homo.concurrent.schedule.Task;
import com.homo.concurrent.schedule.TaskFun;
import com.homo.concurrent.schedule.TaskFun0;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

@Slf4j
public class TimerTaskEvent<T extends Task> extends AbstractBaseEvent{

    TimerTask timerTask;
    T taskFun;
    Object[] params;
    boolean interrupt;

    public  TimerTaskEvent(TimerTask timerTask, T taskFun,  boolean interrupt ,Object... params) {
        this.timerTask = timerTask;
        this.taskFun = taskFun;
        this.params = params;
        this.interrupt = interrupt;
    }

    @Override
    public void process() {
        try {
            if (taskFun instanceof TaskFun0){
                ((TaskFun0)taskFun).run();
            }else {
                ((TaskFun)taskFun).run(params);
            }

        }catch (Exception e){
            log.error("TimerEvent error",e);
            if (interrupt){
                timerTask.cancel();
            }
        }
    }
}
