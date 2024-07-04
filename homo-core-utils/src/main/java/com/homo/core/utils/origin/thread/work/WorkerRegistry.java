package com.homo.core.utils.origin.thread.work;


import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;

/**
 * 工作线程注册管理器。管理注册的线程。
 * 统一进行启动与退出
 */
@Slf4j
public class WorkerRegistry {
    /**
     * 保存注册的Worker
     */
    private final ConcurrentHashMap<String, Worker> workers=new ConcurrentHashMap<>();
    /**
     * 线程同步组件
     */
    private final Phaser phaser=new Phaser();

    public WorkerRegistry() {
    }

    /**
     * 初始化，并注册workers
     * @param workers
     */
    public WorkerRegistry(Worker ...workers) {
         register(workers);
    }

    /**
     * 根据名称获取对应的worker，如果不存在返回null
     * @param name
     * @return
     */
    public Worker getWorker(String name){
        return workers.get(name);
    }

    /**
     * 获取当前worker总数量
     * @return
     */
    public int workerCount(){
        return workers.size();
    }

    /**
     * 判断名称为name的worker是否存在
     * @param name
     * @return
     */
    public boolean containsWorker(String name){
        return workers.containsKey(name);
    }

    /**
     * 判断worker是否已添加。name相同或worker相同都算同一个worker
     * @param worker
     * @return
     */
    public boolean containsWorker(Worker worker){
        return workers.containsKey(worker.getName()) || workers.containsValue(worker);
    }

    /**
     * 获取所有worker的名称
     * @return
     */
    public List<String> workerNames(){
        return new ArrayList<>(workers.keySet());
    }
    /**
     * 注册多个worker
     * @param workers
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public final void register(Worker... workers) throws IllegalArgumentException,NullPointerException {
        if(workers!=null && workers.length>0){
            for (Worker worker : workers) {
                register(worker);
            }
        }
    }
    /**
     * 注册多个worker
     * @param workers
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public final void registerAndStart(Worker... workers) throws IllegalArgumentException,NullPointerException {
        if(workers!=null && workers.length>0){
            for (Worker worker : workers) {
                register(worker);
            }
            for (Worker worker : workers) {
                worker.start();
            }
        }
    }
    /**
     * 注册一个worker
     * @param worker
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public void register(Worker worker) throws IllegalArgumentException,NullPointerException {
        register(worker.getName(),worker);
    }
    /**
     * 注册一个worker并启动它
     * @param worker
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public void registerAndStart(Worker worker) throws IllegalArgumentException,NullPointerException {
        register(worker.getName(),worker);
        worker.start();
    }
    /**
     * 以指定名称注册一个worker
     * @param name
     * @param worker
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public void register(String name, Worker worker) throws IllegalArgumentException,NullPointerException {

        if (worker == null) {
            throw new NullPointerException("worker == null");
        }

        final Worker existing = workers.putIfAbsent(name, worker);
        if (existing != null) {
            throw new IllegalArgumentException("A worker named " + name + " already exists");
        }
        worker.registerPhaser(this.phaser);
    }
    /**
     * 以指定名称注册一个worker，并启动它
     * @param name
     * @param worker
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public void registerAndStart(String name, Worker worker) throws IllegalArgumentException,NullPointerException {
         register(name,worker);
         worker.start();
    }


    /**
     * 开启所有的worker
     * @throws Exception
     */
    public void start() throws Exception {
        if(workerCount()>0) {
            workers.forEach( (name,worker) -> {
                if (!worker.isStarted() && !worker.isStopped() && !worker.isTerminated()) {
                    worker.start();
                }
            });
            log.info("Worker count:{} starting", workers.size());
        }else{
            log.warn("Worker count is zero.");
        }
    }

    /**
     * 关闭所有的worker，并同步等待所有worker退出成功后再退出此方法
     */
    public void stop(boolean interrupt) {

        if(workerCount()>0) {
            phaser.register();
            workers.forEach( (name,worker) -> {
                if (!worker.isStopped() && !worker.isTerminated()) {
                    worker.stop(interrupt);
                }
            });
            log.info("Worker count:{} stopped", workers.size());
            //等待所有线程退出
            phaser.arriveAndAwaitAdvance();
            log.info("Worker count:{} terminated",workers.size());
        }else{
            log.warn("Worker count is zero.");
        }
    }
}
