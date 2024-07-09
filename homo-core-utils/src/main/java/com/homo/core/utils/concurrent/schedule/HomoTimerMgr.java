package com.homo.core.utils.concurrent.schedule;

import com.homo.core.utils.concurrent.queue.CallQueue;
import com.homo.core.utils.concurrent.thread.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

;

@Slf4j
public class HomoTimerMgr {
    /**
     * 任务无限执行次数
     */
    public static final int UNLESS_TIMES = 0;
    public static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
    private ScheduledExecutorService timer;

    private HomoTimerMgr() {
    }

    private static volatile HomoTimerMgr homoTimerMgr;

    public static HomoTimerMgr getInstance() {
        if (homoTimerMgr == null) {
            synchronized (HomoTimerMgr.class) {
                if (homoTimerMgr == null) {
                    homoTimerMgr = new HomoTimerMgr();
                }
            }
        }
        return homoTimerMgr;
    }

    public ScheduledExecutorService getTimer() {
        if (timer == null) {
            synchronized (HomoTimerMgr.class) {
                if (timer == null) {
                    timer = Executors.newScheduledThreadPool(4, ThreadPoolFactory.newThreadFactory("timerPool"));
                }
            }
        }
        return timer;
    }


    private void schedule(HomoTimerTask timerTask, long date, long period) {
        try {
            ScheduledFuture<?> future = getTimer().scheduleAtFixedRate(timerTask, date, period, TimeUnit.MILLISECONDS);
            timerTask.future = future;
        } catch (IllegalStateException cancelled) {
            timer = null;
            log.error("timerTask cancelled params {} runTimes {} currentTimes {}!", timerTask.objects, timerTask.runTimes, timerTask.currentTimes);
        } catch (Throwable throwable) {
            throw throwable;
        }
    }
    public HomoTimerTask once(Runnable taskFun, String time, Object... objects) throws ParseException {
        return once(UUID.randomUUID().toString(),taskFun,time,objects);
    }
    public HomoTimerTask once(String id,Runnable taskFun, String time, Object... objects) throws ParseException {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id, taskFun, 1, objects);
        schedule(homoTimerTask, dateFormat.parse(time).getTime() - System.currentTimeMillis(), 1);
        return homoTimerTask;
    }
    public HomoTimerTask once(Runnable taskFun, Date time, Object... objects) {
        return once(UUID.randomUUID().toString(),taskFun,time,objects);
    }
    public HomoTimerTask once(String id,Runnable taskFun, Date time, Object... objects) {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id, taskFun, 1, objects);
        schedule(homoTimerTask, time.getTime() - System.currentTimeMillis(), 1);
        return homoTimerTask;
    }

    public HomoTimerTask once(Runnable taskFun, long delayMillSecond, Object... objects) {
        return once(UUID.randomUUID().toString(),taskFun, delayMillSecond,objects);
    }

    public HomoTimerTask once(String id, Runnable taskFun, long delayMillSecond, Object... objects) {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id, taskFun, 1, objects);
        schedule(homoTimerTask, delayMillSecond, 1);
        return homoTimerTask;
    }

    public HomoTimerTask once(String id, CallQueue callQueue, Runnable taskFun, long delayMillSecond, Object... objects) {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id,callQueue, taskFun, 1, objects);
        schedule(homoTimerTask, delayMillSecond, 1);
        return homoTimerTask;
    }

    public HomoTimerTask schedule(Runnable taskFun, String time, long period, int runTimes, Object... objects) throws ParseException {
        return schedule(UUID.randomUUID().toString(),taskFun,time,period,runTimes,objects);
    }

    public HomoTimerTask schedule(String id, CallQueue callQueue, Runnable taskFun, long delayMillSecond, long period, Object... objects) {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id,callQueue, taskFun, HomoTimerTask.ENDLESS, objects);
        schedule(homoTimerTask, delayMillSecond, period);
        return homoTimerTask;
    }

    public HomoTimerTask schedule(String id,Runnable taskFun, String time, long period, int runTimes, Object... objects) throws ParseException {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id, taskFun, runTimes, objects);
        schedule(homoTimerTask, dateFormat.parse(time).getTime() - System.currentTimeMillis(), period);
        return homoTimerTask;
    }

    public HomoTimerTask schedule(Runnable taskFun, Date time, long period, int runTimes, Object... objects) {
        return schedule(UUID.randomUUID().toString(),taskFun,time,period,runTimes,objects);
    }

    public HomoTimerTask schedule(String id,Runnable taskFun, Date time, long period, int runTimes, Object... objects) {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id, taskFun, runTimes, objects);
        schedule(homoTimerTask, time.getTime() - System.currentTimeMillis(), period);
        return homoTimerTask;
    }

    public HomoTimerTask schedule(Runnable taskFun, long delayMillSecond, long period, int runTimes, Object... objects) {
        return schedule(UUID.randomUUID().toString(),taskFun, delayMillSecond,period,runTimes,objects);
    }
    public HomoTimerTask schedule(String id, Runnable taskFun, long delayMillSecond, long period, int runTimes, Object... objects) {
        HomoTimerTask homoTimerTask = new HomoTimerTask(id, taskFun, runTimes, objects);
        schedule(homoTimerTask, delayMillSecond, period);
        return homoTimerTask;
    }

}
