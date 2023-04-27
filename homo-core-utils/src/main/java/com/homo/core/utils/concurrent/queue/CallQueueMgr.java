package com.homo.core.utils.concurrent.queue;

import brave.internal.Nullable;
import com.homo.core.common.module.Module;
import com.homo.core.utils.concurrent.event.Event;
import com.homo.core.utils.concurrent.thread.ThreadPoolFactory;
import com.homo.core.utils.rector.Homo;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

;

/**
 * 事件处理管理器
 */
@Log4j2
public class CallQueueMgr implements Module {
    public static final String DEFAULT_TYPE = "defaultPloyType";
    private volatile static CallQueueMgr instance = null;
    private static final int queueCount = Integer.parseInt(System.getProperty("call.queue.count", "4"));
    private static final int waitNum = Integer.parseInt(System.getProperty("call.queue.maxWaitNum", "10000"));
    private static final int keepLive = Integer.parseInt(System.getProperty("call.queue.maxWaitNum", "10"));
    Map<String, BiFunction<Event, Object, Integer>> ployFunMap = new ConcurrentHashMap<>();
    CallQueue[] callQueues;
    public ExecutorService executorService;
    ThreadLocal<CallQueue> localQueue = new ThreadLocal<>();
    public static final int frame_queue_id = 0;
    public static final int user_queue_id = 1;
    public static final BiFunction<Event, Object, Integer> robinPloyFun = new BiFunction<Event, Object, Integer>() {
        int index = 0;

        @Override
        public Integer apply(Event event, Object param) {
            if (param instanceof CallQueueProducer){
                Integer queueId = ((CallQueueProducer) param).getQueueId();
                if (queueId == null) {
                    log.error("getQueueId error in param [{}] :", param);
                }
                return queueId;
            }
            if (event instanceof CallQueueProducer){
                Integer queueId = ((CallQueueProducer) param).getQueueId();
                if (queueId == null) {
                    log.error("getQueueId error in event [{}] :", param);
                }
                return queueId;
            }
            if (index >= queueCount) {
                //绕开主线程 0 和框架线程 1 //todo 分队列优先级
                index = 1;
            }
            return index++;
        }
    };



    private CallQueueMgr() {
    }

    public  void init() {
        log.debug("CallQueueMgr init");
        getInstance();
    }

    public static CallQueueMgr getInstance() {
        if (instance == null) {
            synchronized (CallQueueMgr.class) {
                if (instance == null) {
                    CallQueueMgr newInstance = new CallQueueMgr();
                    newInstance.start(robinPloyFun);
                    instance = newInstance;
                }
            }
        }
        return instance;
    }

    private void start(BiFunction<Event, Object, Integer> ployFun) {
        callQueues = new CallQueue[queueCount];
        registerPloy(DEFAULT_TYPE, robinPloyFun);
        executorService = ThreadPoolFactory.newThreadPool("CallQueueMgrPool", queueCount, keepLive);
        for (int i = 0; i < queueCount; i++) {
            CallQueue callQueue = new CallQueue(i, waitNum);
            callQueue.start(this);
            callQueues[i] = callQueue;
        }
        setLocalQueue(callQueues[0]);//设置主线程队列未第一个队列
        checkInitFinish();
    }

    void checkInitFinish() {
        int finishCount = 0;
        for (CallQueue callQueue : callQueues) {
            if (!callQueue.isRunning()) {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            checkInitFinish();
                        } catch (Throwable throwable) {
                            log.error("checkInitFinish error!", throwable);
                            System.exit(-1);
                        }
                        cancel();
                    }
                }, 1000, 1);
                break;
            }
            finishCount++;
        }
        if (callQueues.length <= 0) {
            log.error("callQueues is empty!!!");
        } else if (finishCount < callQueues.length) {
            log.warn("CallQueueMgr init>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>({}/{})", finishCount, callQueues.length);
        } else {
            log.info("CallQueueMgr init>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>({}/{}) finish!", finishCount, callQueues.length);
        }
    }

    public void setLocalQueue(CallQueue callQueue) {
        log.info("CallQueueMgr.setLocalQueue");
        localQueue.set(callQueue);
    }

    public CallQueue getLocalQueue() {
        CallQueue queue = localQueue.get();
        if (queue == null) {
            log.warn("CallQueueMgr.getLocalQueue error! [{}]:", Thread.currentThread());
        }
        return queue;
    }

    public void registerPloy(String ployType, BiFunction<Event, Object, Integer> ployFun) {
        ployFunMap.put(ployType, ployFun);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public int getQueueCount() {
        return callQueues.length;
    }

    //根据seed选择QueueId
    public int choiceQueueIdBySeed(int seed) {
        seed = Math.abs(seed);
        return seed % callQueues.length;
    }

    //根据seed选择QueueId
    synchronized int choiceQueueIdByPloy(@NonNull BiFunction<Event, Object, Integer> ployFun, Event e, Object param) {
        return Math.abs(ployFun.apply(e, param));
    }

    //将hashCode作为选择CallQueue的种子
    public CallQueue choiceQueueByHashCode(int hashCode) {
        return getQueue(choiceQueueIdBySeed(hashCode));
    }

    //index跟queueId一一对应
    public CallQueue getQueue(int index) {
        if (index >= callQueues.length) {
            log.error("getQueue index:{} >= callQueues.length:{}", index, callQueues.length);
            return null;
        }
        return callQueues[index];
    }

    public void addEvent(Event e) {
        addEvent(DEFAULT_TYPE, e);
    }

    public void addEvent(String ployType, Event e) {
        addEvent(ployType, e, null);
    }

    public void addEvent(Event e, Object param) {
        addEvent(DEFAULT_TYPE, e, param);
    }

    public void addEvent(String ployType, Event e, Object param) {
        getQueue(choiceQueueIdByPloy(ployFunMap.get(ployType), e, param)).addEvent(e);
    }

    public void addEvent(int queueId, Event e) {
        getQueue(queueId).addEvent(e);
    }

    /**
     * 通过队列执行一个任务，随机分配一个队列执行
     */
    public void task(Runnable runnable) {
        addEvent(runnable::run);
    }

    /**
     * 通过队列执行一个任务，系统任务分配到一个指定队列执行（默认队列1）
     */
    public void frameTask(Runnable runnable) {
        getQueue(frame_queue_id).addEvent(runnable::run);
    }


    /**
     * 在指定的队列，执行一个任务，一般用作自己计算队列ID的情况
     *
     * @param runnable 任务
     * @param queueId  队列id
     */
    public void task(Runnable runnable, int queueId) {
        getQueue(queueId).addEvent(runnable::run);
    }

    /**
     * 在callQueueProducer所在的线程执行一个任务
     *
     * @param runnable          需要执行的任务
     * @param callQueueProducer 任务执行者
     */
    public void task(Runnable runnable, CallQueueProducer callQueueProducer) {
        addEvent(runnable::run, callQueueProducer);
    }

    // 执行一个任务，异步获得一个返回
    public <R> Homo<R> call(Callable<R> callable){
        return call(callable, null);
    }

    /**
     * 在指定的队列，执行一个任务，异步返回结果
     */
    public <R> Homo<R> call(Callable<R> callable, int queueId) {
        return Homo.warp(sink -> task(() -> {
            try {
                sink.success(callable.call());
            } catch (Exception e) {
                sink.error(e);
            }
        }, queueId));
    }

    /**
     *根据callQueueProducer执行一个任务，异步返回结果
     */
    public <R> Homo<R>  call(Callable<R> callable,@Nullable CallQueueProducer callQueueProducer){
        return Homo.warp(sink ->
                task(()->{
                    try {
                        sink.success(callable.call());
                    } catch (Exception e) {
                        sink.error(e);
                    }
                }, callQueueProducer)
        );
    }

    public int getAllWaitCount() {
        int count = 0;
        for (CallQueue callQueue : callQueues) {
            count+= callQueue.getWaitingTasksNum();
        }
        return count;
    }
}
