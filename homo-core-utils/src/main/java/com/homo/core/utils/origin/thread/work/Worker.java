package com.homo.core.utils.origin.thread.work;

import java.util.concurrent.Phaser;

/**
 * 工作线程接口。由WorkerRegistry进行管理的工作线程
 */
public interface Worker {
    /**
     *获取工作线程名称
     */
    String getName();
    /**
     *启动线程
     */
    void start() ;

    /**
     * 线程是否已被调用过启动
     * @return boolean
     */
    boolean isStarted();

    /**
     * 停止线程。
     * 如果interrupt=false,只是设置stop变量为true
     * 如果interrupt=true,除了设置stop变量为true,还会调用线程的interrupted方法
     */
    void stop(boolean interrupt);

    /**
     * 阻塞等待消费者退出
     */
    void waitStopped();

    /**
     * 线程是否已被调用过停止
     * @return boolean
     */
    boolean isStopped();

    /**
     * 线程是否终止了
     * @return boolean
     */
    boolean isTerminated();

    /**
     * 注册phaser
     * @param phaser
     */
    void registerPhaser(Phaser phaser);
}
