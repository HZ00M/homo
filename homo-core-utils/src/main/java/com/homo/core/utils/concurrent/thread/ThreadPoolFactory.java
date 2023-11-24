package com.homo.core.utils.concurrent.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public class ThreadPoolFactory {
    static ExecutorService executorService = null;
    static int factoryCount = 0;

    public static ExecutorService newThreadPool() {
        return newThreadPool(null, null, null);
    }

    public static ExecutorService newThreadPool(String poolName) {
        return newThreadPool(poolName, null, null, null);
    }

    public static ExecutorService newThreadPool(String poolName, Integer corePoolSize) {
        return newThreadPool(poolName, corePoolSize, null, null);
    }

    public static ExecutorService newThreadPool(String poolName, Integer corePoolSize, Integer keepAlive) {
        return newThreadPool(poolName, corePoolSize, null, keepAlive);
    }

    public static ExecutorService newThreadPool(String poolName, Integer corePoolSize, Integer maxPoolSize, Integer keepAlive) {
        return newThreadPoolWithQueueSize(poolName, corePoolSize, null, maxPoolSize, keepAlive, null);
    }

    public static ExecutorService newThreadPoolWithQueueSize(String poolName, Integer corePoolSize, Integer queueSize, Integer maxPoolSize, Integer keepAlive) {
        return newThreadPoolWithQueueSize(poolName, corePoolSize, queueSize, maxPoolSize, keepAlive, null);
    }

    public static ExecutorService newThreadPoolWithQueueSize(String poolName, Integer corePoolSize, Integer queueSize, Integer maxPoolSize, Integer keepAlive, ThreadFactory factory) {
        log.info("newThreadPoolWithQueueSize preset poolName {} corePoolSize {} queueSize {} maxPoolSize {} keepAlive {}/second ", poolName, corePoolSize, queueSize, maxPoolSize, keepAlive);
        String noNullPoolName = Optional.ofNullable(poolName).orElse("DefaultThreadPoolFactory");
        Integer noNullCorePoolSize = Optional.ofNullable(corePoolSize).orElse(Runtime.getRuntime().availableProcessors());
        Integer noNullMaxPoolSize = Optional.ofNullable(maxPoolSize).orElse(noNullCorePoolSize);
        Integer noNullKeepAlive = Optional.ofNullable(keepAlive).orElse(Integer.MAX_VALUE);
        LinkedBlockingDeque linkedBlockingDeque = queueSize == null ? new LinkedBlockingDeque<>() : new LinkedBlockingDeque<>(queueSize);
        ThreadFactory threadFactory = Optional.ofNullable(factory).orElse(newThreadFactory(noNullPoolName));
        log.info    ("newThreadPoolWithQueueSize set poolName {} corePoolSize {} queueSize {} maxPoolSize {} keepAlive {}/second ", noNullPoolName, noNullCorePoolSize, "limitless", noNullMaxPoolSize, noNullKeepAlive);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(noNullCorePoolSize, noNullMaxPoolSize, noNullKeepAlive, TimeUnit.SECONDS, linkedBlockingDeque, threadFactory);
        executorService.prestartAllCoreThreads();
        return executorService;
    }

    public static ThreadFactory newThreadFactory(String threadName) {
        log.warn("newHomoThreadFactory threadName {}", threadName);
        return new ThreadFactory() {
            int threadCount = 0;
            final int factoryIndex = factoryCount++;

            @Override
            public Thread newThread(Runnable r) {
                String name = threadName + "[" + threadCount + "]";
                String info = String.format("newHomoThreadFactory factoryName_[%s]:factoryIndex_[%d]:threadCount_[%d]", name, factoryIndex, threadCount++);
                log.info("new thread [{}]", info);
                Runnable warp = () -> {
                    log.info("run task in {} >>> begin", info);
                    r.run();
                    log.info("new task in {} >>> end", info);
                };
                Thread newThread = new Thread(warp, name);
                return newThread;
            }
        };
    }
}
