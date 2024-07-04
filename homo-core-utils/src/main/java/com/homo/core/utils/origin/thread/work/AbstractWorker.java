package com.homo.core.utils.origin.thread.work;


import com.homo.core.utils.origin.ThreadUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadFactory;

/**
 * 工作线程的实现
 */
public abstract class AbstractWorker implements Worker {
    private final static Logger log= LoggerFactory.getLogger(AbstractWorker.class);
    /**
     * 真实的运行线程
     */
    @Nullable
    protected final Thread workThread;
    /**
     * started:线程是否设置了启动
     * stopped:线程是否设置了关闭
     * terminated:线程是否终止
     */
    private volatile boolean started, stopped, terminated;
    /**
     * 线程的名称。
     */
    private final String name;
    /**
     * 外部设置的同步器。
     */
    private volatile Phaser phaser;

    /**
     * 初始化
     * @param namingPattern 线程名称
     */
    public AbstractWorker(@NotNull String namingPattern) {
        this(namingPattern,null);
    }

    /**
     * 初始化
     * @param namingPattern 线程名称
     * @param uh 错误处理器
     */
    public AbstractWorker(@NotNull String namingPattern , @Nullable Thread.UncaughtExceptionHandler uh) {
        this(ThreadUtil.newNamedThreadFactory(namingPattern,false,uh));
    }
    /**
     * 初始化
     * @param threadFactory 线程工厂
     */
    public AbstractWorker(@NotNull ThreadFactory threadFactory) {
        this.workThread =threadFactory.newThread(() -> {
                    log.info("Worker {} starting",getName());
                    try {
                        register();
                        startHandler();
                        while (!stopped && !Thread.currentThread().isInterrupted()) {
                            processingHandler();
                        }
                        stopHandler();
                    } catch (InterruptedException interruptedException){
                        log.info("Worker {} interruptedException occurred",getName());
                    } catch (Throwable throwable){
                        log.error("Worker {} exception occurred",getName(),throwable);
                        errorHandler(throwable);
                    }finally {
                        stopped =true;
                        terminated =true;
                        arrive();
                    }
                });
        this.name = workThread.getName();
    }
    /**
     * 线程在开始运行前调用此方法
     * @throws Throwable
     */
    public abstract void startHandler() throws Throwable;

    /**
     * 线程循环调用此方法
     * @throws Throwable
     */
    public abstract void processingHandler() throws Throwable;

    /**
     * 线程在结束运行前调用此方法
     * @throws Throwable
     */
    public abstract void stopHandler() throws Throwable;

    /**
     * 线程发生异常时调用此方法
     * @throws Throwable
     */
    public abstract void errorHandler(Throwable throwable);
    /**
     * 开启运行线程
     * @throws IllegalThreadStateException
     */
    @Override
    public synchronized void start() throws IllegalThreadStateException {
        if(started){
            throw new IllegalThreadStateException("Worker "+name+" already been started");
        }
        started =true;
        if(stopped || terminated){
            throw new IllegalThreadStateException("Worker "+name+" already been stopped");
        }
        if(workThread==null){
            throw new NullPointerException("Worker "+name+" is Null");
        }
        workThread.start();
    }

    /**
     * 线程是否已被调用过启动
     * @return boolean
     */
    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * 线程是否已被调用过停止
     * @return boolean
     */
    @Override
    public boolean isStopped() {
        return stopped;
    }

    /**
     * 线程是否终止了
     * @return boolean
     */
    @Override
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * 停止线程。只是设置stop变量为true
     * 如果interrupt=false,只是设置stop变量为true
     * 如果interrupt=true,除了设置stop变量为true,还会调用线程的interrupt()方法     */
    @Override
    public void stop(boolean interrupt) {
        stopped =true;
        if(workThread!=null){
            workThread.interrupt();
        }
        log.warn("Worker {} will stopped ",name );
    }

    /**
     * 阻塞等待消费者退出
     */
    @Override
    public void waitStopped() {
        if(!stopped){
            throw new IllegalThreadStateException("Worker "+name+" stop() must be called before waitForStop() is called");
        }
        if (workThread != null && !workThread.isInterrupted()) {
            while (true) {
                try {
                    workThread.join();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.warn("Worker {} exit completed ",name);
    }

    /**
     * phaser.register
     */
    private void register(){
        if(phaser!=null) {
            phaser.register();
        }
    }

    /**
     * phaser.arrive
     */
    private void arrive(){
        if(phaser!=null) {
            phaser.arrive();
        }
    }

    /**
     * 获取工作线程名称
     * @return String
     */
    @Override
    public String getName() {
        return name;
    }
    //注册phaser
    @Override
    public void registerPhaser(Phaser phaser) {
        this.phaser = Objects.requireNonNull(phaser,"Worker "+ name +" phaser is null!");
    }
}
