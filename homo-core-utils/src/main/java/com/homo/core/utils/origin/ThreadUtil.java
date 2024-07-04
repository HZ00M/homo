package com.homo.core.utils.origin;


import com.homo.core.utils.origin.thread.ExecutorBuilder;
import com.homo.core.utils.origin.thread.NamedThreadFactory;
import com.homo.core.utils.origin.thread.ScheduledExecutorBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 线程池工具
 *
 *
 * 调度流程：
 *
 * （1）如果当前工作线程数量小于核心线程数量，执行器总是优先创建一个新的任务线程，而不是从线程队列中获取一个空闲线程。
 * （2）如果线程池中总的任务数量大于核心线程数量，新接收的任务将会被加入到阻塞队列中，一直对阻塞队列已满。
 *      在核心线程池数量已经用完，阻塞队列没有满的场景下，线程池不会为新任务创建一个新线程（注意，这个逻辑是重点）。
 * （3）当完成一个任务的执行时，执行器总是优先从阻塞队列中获取下一个任务，并开始执行，一直到阻塞队列为空。
 * （4）在核心线程池数量已用完，阻塞队列也已满了的场景下，如果线程池接收到新任务，将为会新任务创建一个新的，非核心线程的线程，并立即执行任务。
 * （5）在核心线程池都用完，阻塞队列也满的情况下，会一直创建新的线程去执行新任务，直到线程池内的线程总数超出maximumPoolSize。
 *      如果线程池的线程总数超过maximumPoolSize，线程池会拒绝接收新任务，当有新任务过来，会为新任务执行拒绝策略。
 *
 *
 * 详细参数：
 *
 * 1、线程池大小
 * corePoolSize:核心线程个数
 * maximumPoolSize:最大线程个数
 * keepAliveTime和unit:空闲线程存活时间
 *
 * corePoolSize:线程池的基本大小，即在没有任务需要执行的时候线程池的大小，并且只有在工作队列满了的情况下才会创建超出这个数量的线程。
 * 这里需要注意的是：在刚刚创建ThreadPoolExecutor的时候，线程并不会立即启动，而是要等到有任务提交时才会启动，
 * 除非调用了prestartCoreThread/prestartAllCoreThreads事先启动核心线程。再考虑到keepAliveTime和allowCoreThreadTimeOut超时参数的影响，
 * 所以没有任务需要执行的时候，线程池的大小不一定是corePoolSize。
 *
 * maximumPoolSize:线程池中允许的最大线程数，线程池中的当前线程数目不会超过该值。如果队列中任务已满，并且当前线程个数小于maximumPoolSize，那么会创建新的线程来执行任务。
 * 这里值得一提的是largestPoolSize，该变量记录了线程池在整个生命周期中曾经出现的最大线程个数。为什么说是曾经呢？因为线程池创建之后，可以调用setMaximumPoolSize()改变运行的最大线程的数目。
 * 一般情况下，当新任务来到的时候，如果当前线程个数小于corePoolSize，就会创建一个新线程来执行该任务，需要说明的是，即使其他线程现在也是空闲的，也会创建新线程。
 * 如果线程个大于等于corePoolSize，那就不会立即创建新线程，它会先尝试加入队列排队，需要强调的是，这经是“尝试”排队，而不是“阻塞等待”入队。
 * 如果队列满了，它就不会排队，而是检查线程个数是否达到了maximumPoolSize，如果没有就继续创建线程，直到线程数达到maximumPoolSize。
 *
 * keepAliveTime:它的目的是为了释放多余的线程资源，它表示，当线程池中的线程个数大于corePoolSize的额外空闲线程的存活时间。也就是说，一个非核心线程，在空闲等待新任务时，
 * 会有一个最长等待时间，即keepAliveTime，如果到了时间还是没有新任务，就会被终止。如果该值为0，则表示所有线程都不会超时终止。
 *
 *
 * 2、线程池队列
 * ThreadPoolExecutor要求线程池队列类型必须是阻塞队列BlockingQueue。
 * LinkedBlockingQueue:基于链表的阻塞队列，可以指定最大长度，但是默认是无界的。
 * ArrayBlockingQueue:基于数组的有界阻塞队列。
 * PriorityBlockingQueue:基于堆的无界阻塞优先级队列。
 * SynchronousQueue:没有实际存储空间的同步阻塞队列。
 * 重点：如果用的是无界队列，需要强调的是，线程个数最多只能达到corePoolSize,到达corePoolSize后，新的任务总会排队，参数maximumPoolSize没有意义。
 * 经过实验，当有界队列的长度大于maximumPoolSize的2倍多一点时，maximumPoolSize也会变到没有意义。所以队列长度，最大设置为maximumPoolSize大小。最小设置为corePoolSize大小性能最好。
 *
 * 3、任务拒绝策略
 * 如果队列有界并且maximumPoolSize有限，则当队列排满时，线程也达到了maximumPoolSize，这时新任务来了，就会触发线程池任务拒绝策略
 * ThreadPoolExecutor定义了四种RejectedExecutionException
 * AbortPolicy:这是默认方式，抛出异常。
 * DiscardPolicy:静默处理，忽略新任务，不抛出异常，也不执行。
 * DiscardOldestPolicy:将等待时间最长的任务扔掉，然后自己排队。
 * CallerRunsPolicy:在任务提交者线程中执行任务，而不是交给线程池中的线程执行。
 *
 *
 * 在很多公司的编程规范中，非常明确的禁止使用Executors快捷创建线程池，是因为Executors会有潜在的BUG。
 * 1.Executors.newFixedThreadPool，Executors.newSingleThreadExecutor中使用的队列是LinkedBlockingQueue<Runnable>（无限阻塞队列），
 *   如果任务提交速度大于处理速度，队列任务无限增长，会造成JVM出现OOM异常，内存耗尽。
 * 2.Executors.newCachedThreadPool，Executors.newScheduledThreadPool的maximumPoolSize参数设置为Integer.MAX_VALUE,而workQueue则为SynchronousQueue<Runnable>,
 *   由于SynchronousQueue是没有容量的同步队列，每一个插入的任务都必须等待删除之后，才能插入新任务，因此新来一个新的任务，调度器都会创建一个新的线程去执行任务。
 *   而新线程的上限为Integer.MAX_VALUE，相当于可以创建无限个线程。如果提交任务够多，会造成JVM出现OOM异常，内存耗尽。
 *
 * 如何确定线程数：
 * 1.IO密集性任务的CPU使用率很低，导致线程空余时间很多，因此通常需要开CPU核心数两倍的线程。当IO线程空闲时，可以启用其他线程继续使用CPU,提高CPU的使用率
 * 2.CPU密集型任务，其特点需要进行大量计算而消耗CPU较多，线程过多反而因为频繁切换线程上下文而导致CPU使用率过低，因此线程数应设置为CPU的核心数
 * 3.混合型任务，即有IO访问又有CPU密集计算。业界有一个成熟的计算公式：
 *   最佳线程数量 = (( 线程等待时间 + 线程使用CPU时间 )) / 线程使用CPU时间 * CPU核心数
 *              = (( 线程等待时间 /  线程使用CPU时间 ) + 1 ) * CPU核心数
 */
public final class ThreadUtil {
	private ThreadUtil() {
	}

	/**
	 * IO密集性任务的CPU使用率很低，导致线程空余时间很多，因此通常需要开CPU核心数两倍的线程。当IO线程空闲时，可以启用其他线程继续使用CPU,提高CPU的使用率
	 * @return
	 */
	public static int ioThreadCount(){
		return ExecutorBuilder.CPU_COUNT * 2;
	}

	/**
	 * CPU密集型任务，其特点需要进行大量计算而消耗CPU较多，线程过多反而因为频繁切换线程上下文而导致CPU使用率过低，因此线程数应设置为CPU的核心数 +1
	 * @return
	 */
	public static int computeThreadCount(){
		return ExecutorBuilder.CPU_COUNT + 1;
	}

	/**
	 * 混合型任务，即有IO访问又有CPU密集计算。业界有一个成熟的计算公式：
	 * 最佳线程数量 = (( 线程等待时间 + 线程使用CPU时间 )) / 线程使用CPU时间 * CPU核心数
	 *            = (( 线程等待时间 /  线程使用CPU时间 ) + 1 ) * CPU核心数
	 * @param waitTime
	 * @param cpuTime
	 * @return
	 */
	public static int mixedThreadCount(int waitTime,int cpuTime){
		return ((waitTime/cpuTime)+1) *ExecutorBuilder.CPU_COUNT;
	}
	/**
	 * 获得一个新的线程池<br>
	 * 默认的配置与CPU计算密集型配置一致。
	 * corePoolSize=CPU核心数+1
	 * maximumPoolSize=corePoolSize
	 * @return {@link ThreadPoolExecutor}
	 */
	public static ThreadPoolExecutor newExecutor() {
		return ExecutorBuilder.create().build();
	}

	public static ThreadPoolExecutor newIoExecutor() {
		return ExecutorBuilder.create()
				.setCorePoolSize(ioThreadCount())
				.setMaxPoolSize(ioThreadCount())
				.build();
	}
	public static ThreadPoolExecutor newComputeExecutor() {
		return ExecutorBuilder.create()
				.setCorePoolSize(computeThreadCount())
				.setMaxPoolSize(computeThreadCount())
				.build();
	}

	public static ThreadPoolExecutor newExecutor(int queueCapacity) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).build();
	}

	public static ThreadPoolExecutor newExecutor(int queueCapacity, RejectedExecutionHandler rejectedExecutionHandler) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setHandler(rejectedExecutionHandler).build();
	}

	public static ThreadPoolExecutor newExecutor(int queueCapacity, ThreadFactory threadFactory) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setThreadFactory(threadFactory).build();
	}

	public static ThreadPoolExecutor newExecutor(long keepAliveTime, TimeUnit timeUnit, int queueCapacity) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setKeepAliveTime(keepAliveTime, timeUnit).build();
	}

	public static ThreadPoolExecutor newExecutor(long keepAliveTime, TimeUnit timeUnit, int queueCapacity, RejectedExecutionHandler rejectedExecutionHandler) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setKeepAliveTime(keepAliveTime, timeUnit).setHandler(rejectedExecutionHandler).build();
	}

	public static ThreadPoolExecutor newExecutor(long keepAliveTime, TimeUnit timeUnit, int queueCapacity, ThreadFactory threadFactory) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setKeepAliveTime(keepAliveTime, timeUnit).setThreadFactory(threadFactory).build();
	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(maximumPoolSize*10)).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).build();
	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).build();
	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity, RejectedExecutionHandler rejectedExecutionHandler) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setHandler(rejectedExecutionHandler).build();
	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, int queueCapacity, ThreadFactory threadFactory) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setThreadFactory(threadFactory).build();

	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueCapacity) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).build();

	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueCapacity, RejectedExecutionHandler rejectedExecutionHandler) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setHandler(rejectedExecutionHandler).build();
	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueCapacity, ThreadFactory threadFactory) {
		return ExecutorBuilder.create().setWorkQueue(new LinkedBlockingQueue<>(queueCapacity)).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setThreadFactory(threadFactory).build();

	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> queue) {
		return ExecutorBuilder.create().setWorkQueue(queue).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).build();

	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> queue, RejectedExecutionHandler rejectedExecutionHandler) {
		return ExecutorBuilder.create().setWorkQueue(queue).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setHandler(rejectedExecutionHandler).build();
	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> queue, ThreadFactory threadFactory) {
		return ExecutorBuilder.create().setWorkQueue(queue).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setThreadFactory(threadFactory).build();
	}

	public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> queue, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
		return ExecutorBuilder.create().setWorkQueue(queue).setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setThreadFactory(threadFactory).setHandler(rejectedExecutionHandler).build();
	}


	/**
	 * 获得一个新的任务线程池<br>
	 * 按当前CPU核心数设置大小
	 *
	 * @return {@link ScheduledThreadPoolExecutor}
	 */
	public static ScheduledThreadPoolExecutor newScheduledExecutor() {
		return ScheduledExecutorBuilder.create().build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(RejectedExecutionHandler rejectedExecutionHandler) {
		return ScheduledExecutorBuilder.create().setHandler(rejectedExecutionHandler).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(ThreadFactory threadFactory) {
		return ScheduledExecutorBuilder.create().setThreadFactory(threadFactory).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(long keepAliveTime, TimeUnit timeUnit) {
		return ScheduledExecutorBuilder.create().setKeepAliveTime(keepAliveTime, timeUnit).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(long keepAliveTime, TimeUnit timeUnit, RejectedExecutionHandler rejectedExecutionHandler) {
		return ScheduledExecutorBuilder.create().setKeepAliveTime(keepAliveTime, timeUnit).setHandler(rejectedExecutionHandler).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(long keepAliveTime, TimeUnit timeUnit, ThreadFactory threadFactory) {
		return ScheduledExecutorBuilder.create().setKeepAliveTime(keepAliveTime, timeUnit).setThreadFactory(threadFactory).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(int corePoolSize, int maximumPoolSize) {
		return ScheduledExecutorBuilder.create().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(int corePoolSize, int maximumPoolSize, RejectedExecutionHandler rejectedExecutionHandler) {
		return ScheduledExecutorBuilder.create().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setHandler(rejectedExecutionHandler).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(int corePoolSize, int maximumPoolSize, ThreadFactory threadFactory) {
		return ScheduledExecutorBuilder.create().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setThreadFactory(threadFactory).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit) {
		return ScheduledExecutorBuilder.create().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).build();

	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, RejectedExecutionHandler rejectedExecutionHandler) {
		return ScheduledExecutorBuilder.create().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setHandler(rejectedExecutionHandler).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, ThreadFactory threadFactory) {
		return ScheduledExecutorBuilder.create().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setThreadFactory(threadFactory).build();
	}

	public static ScheduledThreadPoolExecutor newScheduledExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
		return ScheduledExecutorBuilder.create().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).setKeepAliveTime(keepAliveTime, timeUnit).setThreadFactory(threadFactory).setHandler(rejectedExecutionHandler).build();
	}
	/**
	 * 调用 executor.shutdown 方法关闭线程池, 等待1秒钟,循环检查线程池是否为终止状态。
	 * 直到executor.isTerminated==true才会退出循环。
	 * @param executor
	 * @throws InterruptedException
	 */
	public static  void shutdownWaitTaskCompleted(ExecutorService executor) throws InterruptedException {
		shutdownWaitTaskCompleted(executor,1,TimeUnit.SECONDS);
	}
	/**
	 * 调用 executor.shutdown 方法关闭线程池, 等待seconds秒钟,循环检查线程池是否为终止状态。
	 * 直到executor.isTerminated==true才会退出循环。
	 * @param executor
	 * @param seconds
	 * @throws InterruptedException
	 */
	public static  void shutdownWaitTaskCompleted(ExecutorService executor,int seconds) throws InterruptedException {
		shutdownWaitTaskCompleted(executor,seconds,TimeUnit.SECONDS);
	}
	/**
	 * 调用 executor.shutdown 方法关闭线程池, 等待awaitTerminationTimeOut指定的时间,循环检查线程池是否为终止状态。
	 * 直到executor.isTerminated==true才会退出循环。
	 * @param executor
	 * @param awaitTerminationTimeOut
	 * @param awaitTerminationTimeUnit
	 * @throws InterruptedException
	 */
	public static  void shutdownWaitTaskCompleted(ExecutorService executor,long awaitTerminationTimeOut,TimeUnit awaitTerminationTimeUnit) throws InterruptedException {
		Assert.notNull(executor,"executor is null!");
		Assert.isTrue(awaitTerminationTimeOut>0,"awaitTerminationTimeOut must be greater than 0!");
		Assert.notNull(awaitTerminationTimeUnit,"awaitTerminationTimeUnit is null!");
		if(executor.isTerminated()) {
			return;
		}
		executor.shutdown();
		while(!executor.awaitTermination(awaitTerminationTimeOut,awaitTerminationTimeUnit)){}
	}

	/**
	 * 调用 executor.shutdown 方法关闭线程池, 等待1秒钟,检查线程池是否为终止状态，如果不是，则立即调用executor.shutdownNow方法
	 *  此方法会抛弃队列中还没有完成的任务。会导致任务丢失。
	 * @param executor
	 * @throws InterruptedException
	 */
	public static  void shutdownWithTimeOut(ExecutorService executor) throws InterruptedException {
		shutdownWithTimeOut(executor,1,TimeUnit.SECONDS);
	}
	/**
	 * 调用 executor.shutdown 方法关闭线程池, 等待seconds秒钟,检查线程池是否为终止状态，如果不是，则立即调用executor.shutdownNow方法
	 *  此方法会抛弃队列中还没有完成的任务。会导致任务丢失。
	 * @param executor
	 * @param seconds
	 * @throws InterruptedException
	 */
	public static  void shutdownWithTimeOut(ExecutorService executor,int seconds) throws InterruptedException {
		shutdownWithTimeOut(executor,seconds,TimeUnit.SECONDS);
	}
	/**
	 * 调用 executor.shutdown 方法关闭线程池, 等待awaitTerminationTimeOut指定的时间,检查线程池是否为终止状态，如果不是，则立即调用executor.shutdownNow方法
	 * 如果shutdownNow也没能关闭线程池成功，则会内置循环1000次10毫秒的判断，重复调用shutdownNow方法。因此等待时间会最多比awaitTerminationTimeOut延迟10秒
	 * 此方法会抛弃队列中还没有完成的任务。会导致任务丢失。
	 * @return 未完成的任务
	 * @param executor
	 * @param awaitTerminationTimeOut
	 * @param awaitTerminationTimeUnit
	 * @throws InterruptedException
	 */
	public static Optional<List<Runnable>> shutdownWithTimeOut(ExecutorService executor, long awaitTerminationTimeOut, TimeUnit awaitTerminationTimeUnit) throws InterruptedException {
		Assert.notNull(executor,"executor is null!");
		Assert.isTrue(awaitTerminationTimeOut>0,"awaitTerminationTimeOut must be greater than 0!");
		Assert.notNull(awaitTerminationTimeUnit,"awaitTerminationTimeUnit is null!");
		if(executor.isTerminated()) {
			return Optional.empty();
		}
		executor.shutdown();
		List<Runnable> tasks=null;
		//先按指定时间等待任务完成执行。如果规定时间内还没有任务没完成，则直接关闭任务
		if (!executor.awaitTermination(awaitTerminationTimeOut,awaitTerminationTimeUnit)){
			tasks=executor.shutdownNow();
		}
		//如果shutdownNow还没能关闭任务，则执行1000次，每次10毫秒的等待
		if(!executor.isTerminated()){
			for(int i=0;i<1000;i++){
				if(executor.awaitTermination(10,TimeUnit.MILLISECONDS)){
					break;
				}
				tasks=executor.shutdownNow();
			}
		}
		return Optional.ofNullable(tasks);
	}
	/**
	 * 新建一个CompletionService，调用其submit方法可以异步执行多个任务，最后调用take方法按照完成的顺序获得其结果。<br>
	 * 若未完成，则会阻塞
	 *
	 * @param <T>      回调对象类型
	 * @param executor 执行器 {@link ExecutorService}
	 * @return CompletionService
	 */
	public static <T> CompletionService<T> newCompletionService(ExecutorService executor) {
		return new ExecutorCompletionService<T>(executor);
	}

	/**
	 * 新建一个CountDownLatch，一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。
	 *
	 * @param threadCount 线程数量
	 * @return CountDownLatch
	 */
	public static CountDownLatch newCountDownLatch(int threadCount) {
		return new CountDownLatch(threadCount);
	}

	/**
	 * 挂起当前线程
	 *
	 * @param timeout  挂起的时长
	 * @param timeUnit 时长单位
	 * @return 被中断返回false，否则true
	 */
	public static boolean sleep(Number timeout, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(timeout.longValue());
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	/**
	 * 挂起当前线程
	 *
	 * @param millis 挂起的毫秒数
	 * @return 被中断返回false，否则true
	 */
	public static boolean sleep(Number millis) {
		if (millis == null) {
			return true;
		}

		try {
			Thread.sleep(millis.longValue());
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	/**
	 * 考虑{@link Thread#sleep(long)}方法有可能时间不足给定毫秒数，此方法保证sleep时间不小于给定的毫秒数
	 *
	 * @param millis 给定的sleep时间
	 * @return 被中断返回false，否则true
	 * @see ThreadUtil#sleep(Number)
	 */
	public static boolean safeSleep(Number millis) {
		long millisLong = millis.longValue();
		long done = 0;
		while (done < millisLong) {
			long before = System.currentTimeMillis();
			if (false == sleep(millisLong - done)) {
				return false;
			}
			long after = System.currentTimeMillis();
			done += (after - before);
		}
		return true;
	}

	/**
	 * @return 获得堆栈列表
	 */
	public static StackTraceElement[] getStackTrace() {
		return Thread.currentThread().getStackTrace();
	}

	/**
	 * 获得堆栈项
	 *
	 * @param i 第几个堆栈项
	 * @return 堆栈项
	 */
	public static StackTraceElement getStackTraceElement(int i) {
		StackTraceElement[] stackTrace = getStackTrace();
		if (i < 0) {
			i += stackTrace.length;
		}
		return stackTrace[i];
	}

	/**
	 * 创建本地线程对象
	 *
	 * @param <T>           持有对象类型
	 * @param isInheritable 是否为子线程提供从父线程那里继承的值
	 * @return 本地线程
	 */
	public static <T> ThreadLocal<T> createThreadLocal(boolean isInheritable) {
		if (isInheritable) {
			return new InheritableThreadLocal<>();
		} else {
			return new ThreadLocal<>();
		}
	}

	/**
	 * 结束线程，调用此方法后，线程将抛出 {@link InterruptedException}异常
	 *
	 * @param thread 线程
	 * @param isJoin 是否等待结束
	 */
	public static void interrupt(Thread thread, boolean isJoin) {
		if (null != thread && false == thread.isInterrupted()) {
			thread.interrupt();
			if (isJoin) {
				waitForDie(thread);
			}
		}
	}

	/**
	 * 等待线程结束. 调用 {@link Thread#join()} 并忽略 {@link InterruptedException}
	 *
	 * @param thread 线程
	 */
	public static void waitForDie(Thread thread) {
		boolean dead = false;
		do {
			try {
				thread.join();
				dead = true;
			} catch (InterruptedException e) {
				//ignore
			}
		} while (!dead);
	}

	/**
	 * 获取JVM中与当前线程同组的所有线程<br>
	 *
	 * @return 线程对象数组
	 */
	public static Thread[] getThreads() {
		return getThreads(Thread.currentThread().getThreadGroup().getParent());
	}

	/**
	 * 获取JVM中与当前线程同组的所有线程<br>
	 * 使用数组二次拷贝方式，防止在线程列表获取过程中线程终止<br>
	 * from Voovan
	 *
	 * @param group 线程组
	 * @return 线程对象数组
	 */
	public static Thread[] getThreads(ThreadGroup group) {
		final Thread[] slackList = new Thread[group.activeCount() * 2];
		final int actualSize = group.enumerate(slackList);
		final Thread[] result = new Thread[actualSize];
		System.arraycopy(slackList, 0, result, 0, actualSize);
		return result;
	}

	/**
	 * 获取进程的主线程<br>
	 * from Voovan
	 *
	 * @return 进程的主线程
	 */
	public static Thread getMainThread() {
		for (Thread thread : getThreads()) {
			if (thread.getId() == 1) {
				return thread;
			}
		}
		return null;
	}

	/**
	 * 获取当前线程的线程组
	 *
	 * @return 线程组
	 */
	public static ThreadGroup currentThreadGroup() {
		final SecurityManager s = System.getSecurityManager();
		return (null != s) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
	}
	/**
	 * 创建线程
	 *
	 * @param namingPattern   线程名称，格式化为第几个线程
	 * @return {@link ThreadFactory}
	 */
	public static Thread newNamedThread(String namingPattern,Runnable runnable) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(false).build().newThread(runnable);
	}
	public static Thread newNamedThread(String namingPattern,Runnable runnable,Thread.UncaughtExceptionHandler uh) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(false).uncaughtExceptionHandler(uh).build().newThread(runnable);
	}
	/**
	 * 创建线程
	 *
	 * @param namingPattern   线程名称，格式化为第几个线程
	 * @return {@link ThreadFactory}
	 */
	public static Thread newNamedThread(String namingPattern,Runnable runnable,boolean isDaemon) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(isDaemon).build().newThread(runnable);
	}
	public static Thread newNamedThread(String namingPattern,Runnable runnable,boolean isDaemon,Thread.UncaughtExceptionHandler uh) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(isDaemon).uncaughtExceptionHandler(uh).build().newThread(runnable);
	}
	/**
	 * 创建线程工厂
	 *
	 * @param namingPattern   线程名称，格式化为第几个线程
	 * @return {@link ThreadFactory}
	 */
	public static ThreadFactory newNamedThreadFactory(String namingPattern) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(false).build();
	}
	public static ThreadFactory newNamedThreadFactory(String namingPattern,Thread.UncaughtExceptionHandler uh) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(false).uncaughtExceptionHandler(uh).build();
	}
	/**
	 * 创建线程工厂
	 *
	 * @param namingPattern    线程名称，格式化为第几个线程
	 * @param isDaemon 是否守护线程
	 * @return {@link ThreadFactory}
	 */
	public static ThreadFactory newNamedThreadFactory(String namingPattern, boolean isDaemon) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(isDaemon).build();
	}
	public static ThreadFactory newNamedThreadFactory(String namingPattern, boolean isDaemon,Thread.UncaughtExceptionHandler uh) {
		return new NamedThreadFactory.Builder().namingPattern(namingPattern).daemon(isDaemon).uncaughtExceptionHandler(uh).build();
	}

	/**
	 * 阻塞当前线程，保证在main方法中执行不被退出
	 *
	 * @param obj 对象所在线程
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public static void sync(Object obj) {
		synchronized (obj) {
			try {
				obj.wait();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

}
