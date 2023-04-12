package com.homo.core.utils.concurrent.event;

import brave.Span;
import com.homo.core.utils.concurrent.schedule.AbstractHomoTimerTask;
import com.homo.core.utils.concurrent.schedule.Task;
import com.homo.core.utils.concurrent.schedule.TaskFun;
import com.homo.core.utils.concurrent.schedule.TaskFun0;
import lombok.extern.log4j.Log4j2;

;

@Log4j2
public class TimerTaskEvent<T extends Task> extends AbstractBaseEvent {

    AbstractHomoTimerTask timerTask;
    T taskFun;
    Object[] params;
    boolean interrupt;

    public  TimerTaskEvent(AbstractHomoTimerTask timerTask, T taskFun, boolean interrupt , Object... params) {
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
                timerTask.future.cancel(true);
            }
        }
    }

}
