package com.homo.core.utils.concurrent.queue;

import brave.Span;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.rector.HomoSink;
import com.homo.core.utils.trace.TraceLogUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class IdCallQueue extends ConcurrentLinkedDeque<IdCallQueue.IdTask> {
    public Integer retryCount;
    private String id;
    private ReentrantLock lock = new ReentrantLock();
    private DropStrategy dropStrategy;
    private long timeOutMills;//0默认不做超时判断
    private AtomicLong finishCounter = new AtomicLong(0);
//    private ThreadLocal<Integer> taskCountThreadLocal = new ThreadLocal<>();

    public IdCallQueue(String id) {
        this(id, 5000, DropStrategy.DROP_CURRENT_TASK, 3);
    }

    public IdCallQueue(String id, long timeOutMills, DropStrategy dropStrategy, Integer retryCount) {
        if (timeOutMills <= 0) {
            throw new RuntimeException("IdCallQueue timeOutMills <= 0 " + timeOutMills);
        }
        this.id = id;
        this.timeOutMills = timeOutMills;
        this.dropStrategy = dropStrategy;
        this.retryCount = retryCount;
    }

    public <T> void addIdTask(Callable runnable) {
        addIdTask(runnable, null, null);
    }

    public <T> void addIdTask(Callable callable, HomoSink sink) {
        addIdTask(callable, null, sink);
    }

    public <T> void addIdTask(Callable callable, Runnable errorCallBack, HomoSink sink) {
        Span span = ZipkinUtil.getTracing().tracer().currentSpan();
        IdTask task = new IdTask(this, callable, errorCallBack, span, sink);
        log.info("IdCallQueue addIdTask id {} task {}", id, task.hashCode());
//        if (taskCountThreadLocal.get() != null && taskCountThreadLocal.get() > 0) {//TODO 可重入问题待解决   目前是不允许重入的
//            log.error("addIdTask addIdTask fail because reentry id {} task {}", id, task.hashCode());
//            if (errorCallBack != null){
//                errorCallBack.run();
//            }
//            if (sink != null){
//                sink.error(new RuntimeException("addIdTask task fail because reentry "));
//            }
//            return;
//        }
        add(task);
//        taskCountThreadLocal.set(1);
        IdTask headTask = peek();
        if (headTask == task) {//如果是队首,就直接执行
            log.info("IdCallQueue headTask == task run, queueId {}  task {}", id, task.hashCode());
            task.run();
            return;
        }
        //检查head
        if (headTask != null) {
            if (timeOutMills > 0 && (System.currentTimeMillis() - headTask.getStartTime()) > timeOutMills) {
                //超时了
                log.error("IdCallQueue task timeOut! queueId {} task {} headTask.startTime {} currentTime {} strategy {}",
                        id, headTask.hashCode(), headTask.getStartTime(), System.currentTimeMillis(), dropStrategy);
                //不丢弃策略,就不用cancel当前task,只需返回errCallBack即可
                headTask.onTimeOut();
                if (dropStrategy == DropStrategy.RETRY) {
                    log.info("IdCallQueue headTask.errorCallBack.run() run  id {} hashCode {} task {}", id, callable.hashCode(), task.hashCode());
                    if (retryCount > 0 && headTask.getRunCount() < retryCount) {
                        headTask.run();
                    } else {
                        headTask.runNextTask();
                    }
                }
                //丢弃策略,就需要cancel当前task
                if (headTask.tryCancel()) {
                    if (dropStrategy == DropStrategy.DROP_ALL_TASK) {
                        clear();
                        return;
                    }
                    if (dropStrategy == DropStrategy.DROP_CURRENT_TASK) {
                        headTask.runNextTask();
                    }
                } else {
                    //取消失败,说明task已经在执行或者已完成了,当做没有超时处理
                    log.info("IdCallQueue task cancel fail! id {} maybe is done! task {}", id, task.hashCode());
                }
            }


        }
    }


    public class IdTask implements Runnable {
        IdCallQueue ownerQueue;
        Callable callable;
        Runnable errorCallBack;
        HomoSink homoSink;
        Span span;
        @Getter
        int runCount;
        @Getter
        @Setter
        long startTime;
        State state = State.NEW;

        public IdTask(IdCallQueue ownerQueue, Callable callable, Runnable errorCallBack, Span span) {
            this.ownerQueue = ownerQueue;
            this.callable = callable;
            this.errorCallBack = errorCallBack;
            this.span = span;
        }

        public IdTask(IdCallQueue ownerQueue, Callable callable, Runnable errorCallBack, Span span, HomoSink sink) {
            this.ownerQueue = ownerQueue;
            this.callable = callable;
            this.errorCallBack = errorCallBack;
            this.homoSink = sink;
            this.span = span;
        }

        @Override
        public void run() {
            try {
                TraceLogUtil.setTraceIdBySpan(span, "idTask run " + id);
                log.info("IdCallQueue task star id {} task {} state {}", id, IdTask.this.hashCode(), state);
                if (state == State.CANCEL) {
                    if (homoSink != null) {
                        homoSink.error(new RuntimeException("IdCallQueue task cancel"));
                    }
                    runNextTask();
                    return;
                }
                state = State.PROCESS;
                log.info("IdCallQueue task id {} run start  task {} state {}", id, IdTask.this.hashCode(), state);
                startTime = System.currentTimeMillis();
                Object call = callable.call();
                if (homoSink != null) {
//                    homoSink.success(call);
                    if (call instanceof Homo) {
                        ((Homo) call).consumerValue(ret -> {
                            state = State.FINISH;
                            log.info("IdCallQueue task id {} run end promise task {} state {}", id, IdTask.this.hashCode(), state);
                            homoSink.success(ret);
                            runNextTask();
                        }).start();
                    } else {
                        state = State.FINISH;
                        log.info("IdCallQueue task id {} run end task {} state {}", id, IdTask.this.hashCode(), state);
                        homoSink.success(call);
                        runNextTask();
                    }
                }
            } catch (Exception e) {
                log.error("IdCallQueue task id {} run error task {}", id, IdTask.this.hashCode(), e);
                if (errorCallBack != null) {
                    errorCallBack.run();
                }
                if (homoSink != null) {
                    homoSink.error(e);
                }
                state = State.ERROR;
                runNextTask();
            } finally {
                runCount++;
            }
        }

        public void runNextTask() {
            ownerQueue.finishCounter.incrementAndGet();
//            taskCountThreadLocal.set(0);
            poll();
            IdTask nextTask = peek();
            if (nextTask != null) {
                nextTask.run();
            }
            log.info("IdCallQueue task runNextTask id {} task {} state {} finishCounter {}", id, IdTask.this.hashCode(), state, ownerQueue.finishCounter.get());
        }

        private boolean tryCancel() {
            if (state != State.FINISH && state != State.PROCESS) {
                state = State.CANCEL;
                return true;
            }
            return false;
        }

        public void onTimeOut() {
            errorCallBack.run();
            if (homoSink != null) {
                homoSink.error(new RuntimeException("IdCallQueue task timeOut"));
            }
            state = State.ERROR;
        }
    }

    public enum State {
        NEW,
        PROCESS,
        FINISH,
        CANCEL,
        ERROR,
    }

    public enum DropStrategy {
        RETRY,
        DROP_CURRENT_TASK,
        DROP_ALL_TASK;
    }
}
