package com.homo.core.utils.concurrent.queue;

import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class IdCallQueue extends ConcurrentLinkedDeque<IdCallQueue.IdTask> {
    private String id;
    private ReentrantLock lock = new ReentrantLock();
    private DropStrategy dropStrategy;
    private long timeOutMills;//0默认不做超时判断
    private AtomicLong finishCounter = new AtomicLong(0);

    public IdCallQueue(String id) {
        this(id, 5000, DropStrategy.NONE);
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
        log.info("IdCallQueue addIdTask id {} hashCode {}", id, callable.hashCode());
        IdTask task = new IdTask(this, callable, errorCallBack, sink);
        add(task);
        IdTask headTask = peek();
        if (headTask == task) {//如果是队首,就直接执行
            log.info("IdCallQueue headTask == task run, id {} hashCode {} task {}", id, callable.hashCode(), task.hashCode());
            task.run();
            return;
        }
        //检查head
        if (headTask != null) {
            if (timeOutMills > 0 && (System.currentTimeMillis() - headTask.getStartTime()) > timeOutMills) {
                //超时了
                log.error("IdCallQueue task timeOut! id:{} headTask.startTime {} currentTime {} strategy {}",
                        id, headTask.getStartTime(), System.currentTimeMillis(), dropStrategy);
                //不丢弃策略,就不用cancel当前task,只需返回errCallBack即可
                if (dropStrategy == DropStrategy.NONE) {
                    headTask.callback();
                    log.info("IdCallQueue headTask.errorCallBack.run() run  id {} hashCode {} task {}", id, callable.hashCode(), task.hashCode());
//                    poll();
//                    IdTask nextTask = peek();
//                    if (nextTask != null) {
//                        nextTask.run();
//                        log.info("IdCallQueue dropStrategy == DropStrategy.NONE run !id {} task {}", id, task);
//                        return;
//                    }
                }
                //丢弃策略,就需要cancel当前task
                if (headTask.tryCancel()) {
                    if (dropStrategy == DropStrategy.DROP_ALL_TASK) {
                        clear();
                        return;
                    }
                    if (dropStrategy == DropStrategy.DROP_CURRENT_TASK) {
                        poll();
//                        IdTask nextTask = peek();
//                        if (nextTask != null) {
//                            log.info("IdCallQueue dropStrategy == DropStrategy.DROP_CURRENT_TASK run !id {} task {}", id, task);
//                            nextTask.run();
//                        }
                    }
                } else {
                    //取消失败,说明task已经在执行或者已完成了,当做没有超时处理
                    log.info("IdCallQueue task cancel fail! id {} maybe is done! hashCode {}", id, callable.hashCode());
                }
            }


        }
    }


    public class IdTask implements Runnable {
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
            try {
                log.info("IdCallQueue task star id {} hashCode {} state {}", id, callable.hashCode(), state);
                if (state == State.CANCEL) {
                    if (homoSink != null) {
                        homoSink.error(new RuntimeException("IdCallQueue task cancel"));
                    }
                    IdTask nextTask = peek();
                    if (nextTask != null) {
                        nextTask.run();
                    }
                    return;
                }
                state = State.PROCESS;
                log.info("IdCallQueue task run id {} hashCode {} state {}", id, callable.hashCode(), state);
                startTime = System.currentTimeMillis();
                Object call = callable.call();
                if (homoSink != null) {
//                    homoSink.success(call);
                    if (call instanceof Homo) {
                        ((Homo) call).consumerValue(ret -> {
                            homoSink.success(ret);
                        }).start();
                    } else {
                        homoSink.success(call);
                    }
                }
                state = State.FINISH;
                log.info("IdCallQueue task end id {} hashCode {} state {}", id, callable.hashCode(), state);
            } catch (Exception e) {
                log.error("IdCallQueue run error", e);
                if (errorCallBack != null) {
                    errorCallBack.run();
                }
                if (homoSink != null) {
                    homoSink.error(e);
                }
            } finally {
                ownerQueue.finishCounter.incrementAndGet();
                poll();
                IdTask nextTask = peek();
                if (nextTask != null) {
                    nextTask.run();
                }
            }
        }

        private boolean tryCancel() {
            if (state != State.FINISH && state != State.PROCESS) {
                state = State.CANCEL;
                return true;
            }
            return false;
        }

        public void callback() {
            errorCallBack.run();
            if (homoSink != null) {
                homoSink.error(new RuntimeException("IdCallQueue task callback"));
            }
            state = State.CANCEL;
        }
    }

    public enum State {
        NEW,
        PROCESS,
        FINISH,
        CANCEL;
    }

    public enum DropStrategy {
        NONE,
        DROP_CURRENT_TASK,
        DROP_ALL_TASK;
    }
}
