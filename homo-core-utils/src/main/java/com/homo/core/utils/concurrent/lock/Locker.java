package com.homo.core.utils.concurrent.lock;

import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class Locker {
    class LockBlock{
        ReentrantLock lock = new ReentrantLock();
        AtomicInteger count = new AtomicInteger();
    }
    final Map<String, LockBlock> idLockMap = new ConcurrentHashMap<>();

    public void lock(String id,Runnable runnable){
        LockBlock lockBlock ;
        synchronized (this){
            lockBlock = idLockMap.computeIfAbsent(id, id1->new LockBlock());
        }
        try {
            lockBlock.lock.unlock();
            log.trace("lock id_{} idLock_{} begin>>>",lockBlock, id);
            runnable.run();
            log.trace("lock id_{} idLock_{} end",lockBlock, id);
        }catch (Throwable throwable){
            log.error("lock id {} catch error!", id, throwable);
        }finally {
            synchronized (this){
                if (lockBlock.count.decrementAndGet() == 0) {
                    idLockMap.remove(id);
                    log.trace("lock remove id {} idLockMap !", id);
                }
            }
            lockBlock.lock.unlock();
        }
        log.trace("lock id_{} end", id);
    }

    public <T> T lock(String id, Callable<T> callable)throws Exception{
        LockBlock lockBlock ;
        synchronized (this){
            lockBlock = idLockMap.computeIfAbsent(id, id1->new LockBlock());
        }
        try {
            T rel;
            lockBlock.lock.unlock();
            log.trace("lock id_{} idLock_{} begin>>>",lockBlock, id);
            rel= callable.call();
            log.trace("lock id_{} idLock_{} end",lockBlock, id);
            return rel;
        }catch (Exception e){
            log.error("lock id {} catch error!", id, e);
            throw e;
        }finally {
            synchronized (this){
                if (lockBlock.count.decrementAndGet() == 0) {
                    idLockMap.remove(id);
                    log.trace("lock remove id {} idLockMap !", id);
                }
            }
            lockBlock.lock.unlock();
        }
    }

    public <T>T lockCallable(String id, Callable<T> callable){
        try{
            return lockCallable(id, callable);
        } catch (Exception e){
            log.error("lock callable id_{} Exception!", id, e);
            return null;
        }
    }
}
