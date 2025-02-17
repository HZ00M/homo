package com.homo.core.utils.concurrent.event;

import com.homo.core.utils.concurrent.schedule.AbstractHomoTimerTask;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TimerTaskEvent extends AbstractTraceEvent {

    AbstractHomoTimerTask timerTask;
     Object[] params;
    boolean interrupt;

    public TimerTaskEvent(AbstractHomoTimerTask timerTask, boolean interrupt, Object... params) {
        this.id = String.format("TimerTaskEvent_%s",timerTask.id);
        this.timerTask = timerTask;
        this.params = params;
        this.interrupt = interrupt;
    }


    @Override
    public void process() {
        if (timerTask.isCancel) {
            log.warn("timer already canceled id [{}] ", timerTask.id );
            return;
        }
        try {
            timerTask.runnable.run();
        } catch (Exception e) {
            log.error("TimerEvent error", e);
            if (interrupt) {
                timerTask.future.cancel(true);
            }
        }
    }

}
