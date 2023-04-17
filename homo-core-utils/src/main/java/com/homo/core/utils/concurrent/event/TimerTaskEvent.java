package com.homo.core.utils.concurrent.event;

import com.homo.core.utils.concurrent.schedule.AbstractHomoTimerTask;
import lombok.extern.log4j.Log4j2;

;

@Log4j2
public class TimerTaskEvent extends AbstractBaseEvent {

    AbstractHomoTimerTask timerTask;
    Runnable taskFun;
    Object[] params;
    boolean interrupt;

    public  TimerTaskEvent(AbstractHomoTimerTask timerTask, Runnable taskFun, boolean interrupt , Object... params) {
        this.timerTask = timerTask;
        this.taskFun = taskFun;
        this.params = params;
        this.interrupt = interrupt;
    }

    @Override
    public void process() {
        try {
            taskFun.run();
        }catch (Exception e){
            log.error("TimerEvent error",e);
            if (interrupt){
                timerTask.future.cancel(true);
            }
        }
    }

}
