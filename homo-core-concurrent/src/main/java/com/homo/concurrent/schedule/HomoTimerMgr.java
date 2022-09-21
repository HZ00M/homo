package com.homo.concurrent.schedule;

import com.homo.concurrent.thread.ThreadPoolFactory;
import lombok.extern.log4j.Log4j2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

;

@Log4j2
public class HomoTimerMgr<T extends Task> {

    public static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
    private ScheduledExecutorService timer;
    private HomoTimerMgr(){}
    private static volatile HomoTimerMgr homoTimerMgr;

    public static HomoTimerMgr getInstance(){
        if (homoTimerMgr ==null){
            synchronized (HomoTimerMgr.class){
                if (homoTimerMgr ==null){
                    homoTimerMgr = new HomoTimerMgr();
                }
            }
        }
        return homoTimerMgr;
    }

    public ScheduledExecutorService getTimer(){
        if (timer==null){
            synchronized (HomoTimerMgr.class){
                if (timer==null){
                    timer = Executors.newScheduledThreadPool(4, ThreadPoolFactory.newThreadFactory("HomoTimeMgr-Thread"));
                }
            }
        }
        return timer;
    }


    private void schedule(HomoTimerTask<T> timerTask, long date, long period){
        try {
            ScheduledFuture<?> future = getTimer().scheduleAtFixedRate(timerTask, date, period, TimeUnit.SECONDS);
            timerTask.future = future;
        }catch (IllegalStateException cancelled){
            timer = null;
            log.error("timerTask cancelled params {} runTimes {} currentTimes {}!",timerTask.objects,timerTask.runTimes,timerTask.currentTimes);
        }catch (Throwable throwable){
            throw throwable;
        }
    }

    public HomoTimerTask<T> once(T taskFun,String time,Object ...objects) throws ParseException {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun, 1,objects);
        schedule(homoTimerTask,dateFormat.parse(time).getTime()-System.currentTimeMillis(),1);
        return homoTimerTask;
    }
    public HomoTimerTask<T> once(T taskFun,Date time,Object ...objects) {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,1,objects);
        schedule(homoTimerTask,time.getTime()-System.currentTimeMillis(),1);
        return homoTimerTask;
    }

    public HomoTimerTask<T> once(T taskFun,long delay,Object ...objects){
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,1,objects);
        schedule(homoTimerTask,delay,1);
        return homoTimerTask;
    }

    public HomoTimerTask<T> schedule(T taskFun,String time,long period,int runTimes,Object ...objects) throws ParseException {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,  runTimes,objects);
        schedule(homoTimerTask,dateFormat.parse(time).getTime()-System.currentTimeMillis(),period);
        return homoTimerTask;
    }

    public HomoTimerTask<T> schedule(T taskFun,Date time,long period,int runTimes,Object ...objects)  {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,  runTimes,objects);
        schedule(homoTimerTask,time.getTime()-System.currentTimeMillis(),period);
        return homoTimerTask;
    }

    public HomoTimerTask<T> schedule(T taskFun,long delay,long period,int runTimes,Object ...objects)  {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun, runTimes,objects);
        schedule(homoTimerTask,delay,period);
        return homoTimerTask;
    }

}
