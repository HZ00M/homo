package com.homo.core.utils.callback;

import com.google.common.util.concurrent.*;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class CallbackTaskScheduler extends Thread {
    private ConcurrentLinkedQueue<CallbackTask> executeTaskQueue = new ConcurrentLinkedQueue<>();
    private AtomicInteger atomicInteger = new AtomicInteger();
    private ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            int index = atomicInteger.incrementAndGet();
            Thread t = new Thread(r, "homo-task" + index);
            return t;
        }
    };

    private ExecutorService jPool = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(),
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),threadFactory, new ThreadPoolExecutor.AbortPolicy());

    ListeningExecutorService gPool = MoreExecutors.listeningDecorator(jPool);

    private long sleepTime = 200;// 线程休眠时间

    private static class SingletonHolder {
        private static final CallbackTaskScheduler INSTANCE = new CallbackTaskScheduler();
    }

    public static final CallbackTaskScheduler getInstance(){
        return SingletonHolder.INSTANCE;
    }

    private CallbackTaskScheduler() {
        this.start();
    }

    public static <T> void add(CallbackTask<T> callbackTask){
        getInstance().executeTaskQueue.add(callbackTask);
    }

    @Override
    public void run() {
        while (true) {
            handleTask();// 处理任务
            threadSleep(sleepTime);
        }
    }

    /**
     * 处理任务队列，检查其中是否有任务
     */
    private void handleTask() {

        CallbackTask executeTask = null;
        while (executeTaskQueue.peek() != null) {
            executeTask = executeTaskQueue.poll();
            handleTask(executeTask);
        }
    }

    /**
     * 执行任务操作
     */
    private <R> void handleTask(CallbackTask<R> executeTask) {
        ListenableFuture<R> future = gPool.submit(executeTask::execute);

        Futures.addCallback(future, new FutureCallback<R>() {
            public void onSuccess(R r) {
                executeTask.onBack(r);
            }
            public void onFailure(Throwable t) {
                executeTask.onError(t);
            }
        });

    }

    private void threadSleep(long time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            log.error("线程等待异常:",e);
        }
    }
}
