package com.homo.core.utils.concurrent.queue;

import com.homo.core.utils.rector.HomoSink;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class IdCallQueue extends ConcurrentLinkedDeque<IdCallQueue.IdTask> {
    private String id;
    private ReentrantLock lock = new ReentrantLock();
    private DropStrategy dropStrategy;
    private long timeOutMills;//0默认不做超时判断
    private AtomicLong finishCounter = new AtomicLong(0);

    public IdCallQueue(String id) {
        this(id, 0, DropStrategy.NONE);
    }

    public IdCallQueue(String id, long timeOutMills, DropStrategy dropStrategy) {
        if (timeOutMills <= 0) {
            throw new RuntimeException("IdCallQueue timeOutMills <= 0 " + timeOutMills);
        }
        this.id = id;
        this.timeOutMills = timeOutMills;
        this.dropStrategy = dropStrategy;
    }

    public <T> void addIdTask(Callable runnable) {
        addIdTask(runnable, null, null);
    }

    public <T> void addIdTask(Callable callable, HomoSink sink) {
        addIdTask(callable, null, sink);
    }

    public <T> void addIdTask(Callable callable, Runnable errorCallBack, HomoSink sink) {
        IdTask task = new IdTask(this, callable, errorCallBack, sink);
        add(task);
        IdTask headTask = peek();
        if (headTask == task) {//如果是队首,就直接执行
            task.run();
        }
        //检查超时
        if (headTask != null //队列不为空
                && timeOutMills > 0 //超时时间大于0
                && (System.currentTimeMillis() - headTask.getStartTime()) > timeOutMills) {//超时了
            log.error("IdCallQueue task timeOut! id:{} headTask.startTime:{}, currentTime:{} strategy:{}",
                    id, headTask.getStartTime(), System.currentTimeMillis(), dropStrategy);
            //不丢弃策略,就不用cancel当前task,只需返回errCallBack即可
            if (dropStrategy == DropStrategy.NONE) {
                headTask.errorCallBack.run();
                poll();
                IdTask nextTask = peek();
                if (nextTask != null) {
                    nextTask.run();
                    return;
                }
            }
            //丢弃策略,就需要cancel当前task
            if (headTask.tryCancel()) {
                if (dropStrategy == DropStrategy.DROP_ALL_TASK) {
                    clear();
                    return;
                }
                if (dropStrategy == DropStrategy.DROP_CURRENT_TASK) {
                    poll();
                    IdTask nextTask = peek();
                    if (nextTask != null) {
                        nextTask.run();
                    }
                }
            }
            //取消失败,说明task已经在执行或者已完成了,当做没有超时处理
        }
    }


    public static class IdTask implements Runnable {
        IdCallQueue ownerQueue;
        Callable callable;
        Runnable errorCallBack;
        HomoSink homoSink;
        @Getter
        @Setter
        long startTime;
        State state = State.NEW;

        public IdTask(IdCallQueue ownerQueue, Callable callable, Runnable errorCallBack) {
            this.ownerQueue = ownerQueue;
            this.callable = callable;
            this.errorCallBack = errorCallBack;
        }

        public IdTask(IdCallQueue ownerQueue, Callable callable, Runnable errorCallBack, HomoSink sink) {
            this.ownerQueue = ownerQueue;
            this.callable = callable;
            this.errorCallBack = errorCallBack;
            this.homoSink = sink;
        }

        @Override
        public void run() {
            if (state == State.CANCEL) {
                if (homoSink != null) {
                    homoSink.error(new RuntimeException("IdCallQueue task cancel"));
                }
                return;
            }
            state = State.PROCESS;
            startTime = System.currentTimeMillis();
            try {
                Object call = callable.call();
                if (homoSink != null){
                    homoSink.success(call);
                }
            } catch (Exception e) {
                log.error("IdCallQueue run error", e);
                if (errorCallBack != null) {
                    errorCallBack.run();
                }
                if (homoSink != null){
                    homoSink.error(e);
                }
            } finally {
                state = State.FINISH;
                ownerQueue.finishCounter.incrementAndGet();
            }
        }

        private boolean tryCancel() {
            if (state != State.FINISH && state != State.PROCESS) {
                state = State.CANCEL;
                return true;
            }
            return false;
        }


        public enum State {
            NEW,
            PROCESS,
            FINISH,
            CANCEL;
        }
    }

    public enum DropStrategy {
        NONE,
        DROP_CURRENT_TASK,
        DROP_ALL_TASK;
    }
}
