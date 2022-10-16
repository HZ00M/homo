package com.homo.concurrent.event;

import brave.Span;
import com.homo.concurrent.schedule.AbstractHomoTimerTask;
import com.homo.concurrent.schedule.Task;
import com.homo.concurrent.schedule.TaskFun;
import com.homo.concurrent.schedule.TaskFun0;
import com.homo.core.utils.trace.TraceAble;
import lombok.extern.log4j.Log4j2;

;

@Log4j2
public class TimerTaskEvent<T extends Task> extends AbstractBaseEvent {
    Span span;
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

    @Override
    public void setTraceInfo(Span span) {
        this.span = span;
    }

    @Override
    public Span getTraceInfo() {
        return span;
    }
}
