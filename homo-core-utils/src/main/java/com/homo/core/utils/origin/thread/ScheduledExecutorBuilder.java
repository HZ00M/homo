package com.homo.core.utils.origin.thread;



import com.homo.core.utils.origin.Builder;

import java.util.concurrent.*;

/**
 * {@link ScheduledThreadPoolExecutor} 建造者
 * 
 */
public class ScheduledExecutorBuilder implements Builder<ScheduledThreadPoolExecutor> {
	private static final long serialVersionUID = 1L;

	/** 初始池大小 */
	private int corePoolSize;
	/** 最大池大小（允许同时执行的最大线程数） */
	private int maxPoolSize = Integer.MAX_VALUE;
	/** 线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长 */
	private long keepAliveTime = TimeUnit.SECONDS.toNanos(60);

	/** 线程工厂，用于自定义线程创建 */
	private ThreadFactory threadFactory;
	/** 当线程阻塞（block）时的异常处理器，所谓线程阻塞即线程池和等待队列已满，无法处理线程时采取的策略 */
	private RejectedExecutionHandler handler;
	/** 线程执行超时后是否回收线程 */
	private boolean allowCoreThreadTimeOut;
	/** 调用ScheduledFutureTask.cancel时是否将ScheduledFutureTask重线程此任务队列中删除**/
	private boolean removeOnCancel;

	//如果线程池状态是SHUTDOWN时是否允许执行还未到期的定时任务，或者是否可以继续执行下个周期任务
	//默认false，即取消还未到期或周期的任务，从队列中移除
	private boolean continueExistingPeriodicTasksAfterShutdown;

	//如果线程池状态是SHUTDOWN时是否允许执行延时任务，默认是true即已提交的任务到期后可以执行
	private boolean executeExistingDelayedTasksAfterShutdown = true;
	/**
	 * 设置初始池大小，默认0
	 * 
	 * @param corePoolSize 初始池大小
	 * @return this
	 */
	public ScheduledExecutorBuilder setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
		return this;
	}

	/**
	 * 设置最大池大小（允许同时执行的最大线程数）
	 * 
	 * @param maxPoolSize 最大池大小（允许同时执行的最大线程数）
	 * @return this
	 */
	public ScheduledExecutorBuilder setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	/**
	 * 设置线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长
	 * 
	 * @param keepAliveTime 线程存活时间
	 * @param unit 单位
	 * @return this
	 */
	public ScheduledExecutorBuilder setKeepAliveTime(long keepAliveTime, TimeUnit unit) {
		return setKeepAliveTime(unit.toNanos(keepAliveTime));
	}

	/**
	 * 设置线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长，单位纳秒
	 * 
	 * @param keepAliveTime 线程存活时间，单位纳秒
	 * @return this
	 */
	public ScheduledExecutorBuilder setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
		return this;
	}



	/**
	 * 设置线程工厂，用于自定义线程创建
	 * 
	 * @param threadFactory 线程工厂
	 * @return this
	 * @see  ThreadFactoryBuilder
	 */
	public ScheduledExecutorBuilder setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
		return this;
	}

	/**
	 * 设置当线程阻塞（block）时的异常处理器，所谓线程阻塞即线程池和等待队列已满，无法处理线程时采取的策略
	 * <p>
	 * 此处可以使用JDK预定义的几种策略，见{@link  RejectPolicy}枚举
	 * 
	 * @param handler {@link RejectedExecutionHandler}
	 * @return this
	 * @see  RejectPolicy
	 */
	public ScheduledExecutorBuilder setHandler(RejectedExecutionHandler handler) {
		this.handler = handler;
		return this;
	}

	/**
	 * 设置线程执行超时后是否回收线程
	 * 
	 * @param allowCoreThreadTimeOut 线程执行超时后是否回收线程
	 * @return this
	 */
	public ScheduledExecutorBuilder setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
		return this;
	}
	/**
	 *  调用ScheduledFutureTask.cancel时是否将ScheduledFutureTask重线程此任务队列中删除
	 *
	 * */
	public ScheduledExecutorBuilder setRemoveOnCancel(boolean removeOnCancel) {
		this.removeOnCancel = removeOnCancel;
		return this;
	}

	/**
	 *如果线程池状态是SHUTDOWN时是否允许执行还未到期的定时任务，或者是否可以继续执行下个周期任务
	 * 	默认false，即取消还未到期或周期的任务，从队列中移除
	 */

	public ScheduledExecutorBuilder setContinueExistingPeriodicTasksAfterShutdown(boolean continueExistingPeriodicTasksAfterShutdown) {
		this.continueExistingPeriodicTasksAfterShutdown = continueExistingPeriodicTasksAfterShutdown;
		return this;
	}

	/**
	 * 如果线程池状态是SHUTDOWN时是否允许执行延时任务，默认是true即已提交的任务到期后可以执行
	 */
	public ScheduledExecutorBuilder setExecuteExistingDelayedTasksAfterShutdown(boolean executeExistingDelayedTasksAfterShutdown) {
		this.executeExistingDelayedTasksAfterShutdown = executeExistingDelayedTasksAfterShutdown;
		return this;
	}


	/**
	 * 创建ExecutorBuilder，开始构建
	 * 
	 * @return {@link ScheduledExecutorBuilder}
	 */
	public static ScheduledExecutorBuilder create() {
		return new ScheduledExecutorBuilder();
	}



	/**
	 * 构建ThreadPoolExecutor
	 */
	@Override
	public ScheduledThreadPoolExecutor build() {
		return build(this);
	}

	/**
	 * 构建ThreadPoolExecutor
	 * 
	 * @param builder {@link ScheduledExecutorBuilder}
	 * @return {@link ThreadPoolExecutor}
	 */
	private static ScheduledThreadPoolExecutor build(ScheduledExecutorBuilder builder) {
		final int corePoolSize = builder.corePoolSize;
		final int maxPoolSize = builder.maxPoolSize;
		final long keepAliveTime = builder.keepAliveTime;

		final ThreadFactory threadFactory = (null != builder.threadFactory) ? builder.threadFactory : Executors.defaultThreadFactory();
		RejectedExecutionHandler handler =  (null != builder.handler) ? builder.handler : new ThreadPoolExecutor.AbortPolicy();

		final ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(
				corePoolSize, //
				threadFactory, //
				handler//
		);
		threadPoolExecutor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
		threadPoolExecutor.setMaximumPoolSize(maxPoolSize);
		threadPoolExecutor.setKeepAliveTime(keepAliveTime,TimeUnit.NANOSECONDS);
		threadPoolExecutor.setRemoveOnCancelPolicy(builder.removeOnCancel);
		threadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(builder.continueExistingPeriodicTasksAfterShutdown);
		threadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(builder.executeExistingDelayedTasksAfterShutdown);
		return threadPoolExecutor;
	}
}
