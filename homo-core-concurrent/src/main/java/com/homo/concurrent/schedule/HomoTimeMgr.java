package com.homo.concurrent.schedule;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

@Slf4j
public class HomoTimeMgr<T extends Task> {

    public static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
    private Timer timer;
    private HomoTimeMgr(){}
    private static volatile HomoTimeMgr homoTimeMgr;

    public static HomoTimeMgr getInstance(){
        if (homoTimeMgr==null){
            synchronized (HomoTimeMgr.class){
                if (homoTimeMgr==null){
                    homoTimeMgr = new HomoTimeMgr();
                }
            }
        }
        return homoTimeMgr;
    }

    public Timer getTimer(){
        if (timer==null){
            synchronized (HomoTimeMgr.class){
                if (timer==null){
                    timer = new Timer();
                }
            }
        }
        return timer;
    }


    private void schedule(HomoTimerTask<T> timerTask, Date date, long period){
        try {
            getTimer().schedule(timerTask,date,period);
        }catch (IllegalStateException cancelled){
            timer = null;
            log.error("timerTask cancelled params {} runTimes {} currentTimes {}!",timerTask.objects,timerTask.runTimes,timerTask.currentTimes);
        }catch (Throwable throwable){
            throw throwable;
        }
    }

    public HomoTimerTask<T> once(T taskFun,String time,Object ...objects) throws ParseException {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun, 1,objects);
        schedule(homoTimerTask,dateFormat.parse(time),1);
        return homoTimerTask;
    }
    public HomoTimerTask<T> once(T taskFun,Date time,Object ...objects) {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,1,objects);
        schedule(homoTimerTask,time,1);
        return homoTimerTask;
    }

    public HomoTimerTask<T> once(T taskFun,long delay,Object ...objects){
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,1,objects);
        schedule(homoTimerTask,new Date(System.currentTimeMillis()+delay),1);
        return homoTimerTask;
    }

    public HomoTimerTask<T> schedule(T taskFun,String time,long period,int runTimes,Object ...objects) throws ParseException {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,  runTimes,objects);
        schedule(homoTimerTask,dateFormat.parse(time),period);
        return homoTimerTask;
    }

    public HomoTimerTask<T> schedule(T taskFun,Date time,long period,int runTimes,Object ...objects)  {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun,  runTimes,objects);
        schedule(homoTimerTask,time,period);
        return homoTimerTask;
    }

    public HomoTimerTask<T> schedule(T taskFun,long delay,long period,int runTimes,Object ...objects)  {
        HomoTimerTask<T> homoTimerTask = new HomoTimerTask<T>(taskFun, runTimes,objects);
        schedule(homoTimerTask,new Date(System.currentTimeMillis()+delay),period);
        return homoTimerTask;
    }

}
