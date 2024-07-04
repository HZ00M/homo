package com.homo.core.utils.origin.thread;


import com.homo.core.utils.origin.Builder;

import java.util.concurrent.*;

/**
 * {@link ThreadPoolExecutor} 建造者
 */
public class ExecutorBuilder implements Builder<ThreadPoolExecutor> {
	private static final long serialVersionUID = -336606475958190928L;
	public static final int CPU_COUNT =Runtime.getRuntime().availableProcessors();
	/**
	 * 默认核心线程数是CPU核心数+1. 默认为CPU计算密集型任务池
	 */
	public static final  int DEFAULT_CORE_POOL_SIZE= CPU_COUNT + 1;
	/**
	 * maxPoolSize 最大线程数在生产环境上我们往往设置成corePoolSize一样，这样可以减少在处理过程中创建线程的开销。
	 */
	public static final  int DEFAULT_MAX_POOL_SIZE= DEFAULT_CORE_POOL_SIZE;
	public static final long DEFAULT_KEEP_ALIVE_TIME_NANO=TimeUnit.SECONDS.toNanos(60);
	public static final ThreadFactory DEFAULT_THREAD_FACTORY=Executors.defaultThreadFactory();
	public static final  RejectedExecutionHandler DEFAULT_REJECTED_EXECUTION_HANDLER= RejectPolicy.CALLER_RUNS.getValue();

	/**
	 * 初始池大小
	 */
	private int corePoolSize=DEFAULT_CORE_POOL_SIZE;
	/**
	 * 最大池大小（允许同时执行的最大线程数）
	 */
	private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
	/**
	 * 线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长,单位纳秒
	 */
	private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME_NANO;
	/**
	 * 队列，用于存在未执行的线程
	 */
	private BlockingQueue<Runnable> workQueue=null;
	/**
	 * 线程工厂，用于自定义线程创建
	 */
	private ThreadFactory threadFactory=DEFAULT_THREAD_FACTORY;
	/**
	 * 当线程阻塞（block）时的异常处理器，所谓线程阻塞即线程池和等待队列已满，无法处理线程时采取的策略
	 */
	private RejectedExecutionHandler handler=DEFAULT_REJECTED_EXECUTION_HANDLER;
	/**
	 * 线程执行超时后是否回收线程
	 */
	private boolean allowCoreThreadTimeOut=false;

	/**
	 * 设置初始池大小，默认0
	 *
	 * @param corePoolSize 初始池大小
	 * @return this
	 */
	public ExecutorBuilder setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
		return this;
	}

	/**
	 * 设置最大池大小（允许同时执行的最大线程数）
	 *
	 * @param maxPoolSize 最大池大小（允许同时执行的最大线程数）
	 * @return this
	 */
	public ExecutorBuilder setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	/**
	 * 设置线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长
	 *
	 * @param keepAliveTime 线程存活时间
	 * @param unit          单位
	 * @return this
	 */
	public ExecutorBuilder setKeepAliveTime(long keepAliveTime, TimeUnit unit) {
		return setKeepAliveTime(unit.toNanos(keepAliveTime));
	}

	/**
	 * 设置线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长，单位纳秒
	 *
	 * @param keepAliveTime 线程存活时间，单位纳秒
	 * @return this
	 */
	public ExecutorBuilder setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
		return this;
	}

	/**
	 * 设置队列，用于存在未执行的线程<br>
	 * 可选队列有：
	 *
	 * <pre>
	 * 1. SynchronousQueue    它将任务直接提交给线程而不保持它们。当运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
	 * 2. LinkedBlockingQueue 无界队列，当运行线程大于corePoolSize时始终放入此队列，此时maximumPoolSize无效
	 * 3. ArrayBlockingQueue  有界队列，相对无界队列有利于控制队列大小，队列满时，运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
	 * </pre>
	 *
	 * @param workQueue 队列
	 * @return this
	 */
	public ExecutorBuilder setWorkQueue(BlockingQueue<Runnable> workQueue) {
		this.workQueue = workQueue;
		return this;
	}

	/**
	 * 使用{@link SynchronousQueue} 做为等待队列（非公平策略）<br>
	 * 它将任务直接提交给线程而不保持它们。当运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
	 *
	 * @return this
	 */
	public ExecutorBuilder useSynchronousQueue() {
		return useSynchronousQueue(false);
	}

	/**
	 * 使用{@link SynchronousQueue} 做为等待队列<br>
	 * 它将任务直接提交给线程而不保持它们。当运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
	 *
	 * @param fair 是否使用公平访问策略
	 * @return this
	 */
	public ExecutorBuilder useSynchronousQueue(boolean fair) {
		return setWorkQueue(new SynchronousQueue<Runnable>(fair));
	}

	/**
	 * 设置线程工厂，用于自定义线程创建
	 *
	 * @param threadFactory 线程工厂
	 * @return this
	 * @see  ThreadFactoryBuilder
	 */
	public ExecutorBuilder setThreadFactory(ThreadFactory threadFactory) {
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
	public ExecutorBuilder setHandler(RejectedExecutionHandler handler) {
		this.handler = handler;
		return this;
	}

	/**
	 * 设置线程执行超时后是否回收线程
	 *
	 * @param allowCoreThreadTimeOut 线程执行超时后是否回收线程
	 * @return this
	 */
	public ExecutorBuilder setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
		return this;
	}

	/**
	 * 创建ExecutorBuilder，开始构建
	 *
	 * @return {@link ExecutorBuilder}
	 */
	public static ExecutorBuilder create() {
		return new ExecutorBuilder();
	}

	/**
	 * 构建ThreadPoolExecutor
	 */
	@Override
	public ThreadPoolExecutor build() {
		return build(this);
	}

	/**
	 * 构建ThreadPoolExecutor
	 *
	 * @param builder {@link ExecutorBuilder}
	 * @return {@link ThreadPoolExecutor}
	 */
	private static ThreadPoolExecutor build(ExecutorBuilder builder) {
		//核心数不允许小于0的设置
		final int corePoolSize = builder.corePoolSize>0? builder.corePoolSize: DEFAULT_CORE_POOL_SIZE;
		final int maxPoolSize = builder.maxPoolSize>0?  builder.maxPoolSize:DEFAULT_MAX_POOL_SIZE;
		final long keepAliveTime = builder.keepAliveTime>=0? builder.keepAliveTime:DEFAULT_KEEP_ALIVE_TIME_NANO;

		final BlockingQueue<Runnable> workQueue;
		if (null != builder.workQueue) {
			workQueue = builder.workQueue;
		} else {
			//默认队列长度为CPU最大核心数的10倍。如果此值大于128，则取128.
			int queueSize=Math.min(maxPoolSize*10,128);
			workQueue = new LinkedBlockingQueue<>(queueSize);
		}
		final ThreadFactory threadFactory = (null != builder.threadFactory) ? builder.threadFactory : DEFAULT_THREAD_FACTORY;
		final RejectedExecutionHandler handler = (null != builder.handler) ? builder.handler : DEFAULT_REJECTED_EXECUTION_HANDLER;

		final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(//
				corePoolSize, //
				maxPoolSize, //
				keepAliveTime, TimeUnit.NANOSECONDS, //
				workQueue, //
				threadFactory, //
				handler//
		);
		threadPoolExecutor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);

		return threadPoolExecutor;
	}
}