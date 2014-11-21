package com.vteba.netty.server;

import io.netty.util.concurrent.AbstractEventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对ChannelGroup进行群操作时，需要一个EventExecutor，但是Netty只提供了单线程的，
 * 这里重写了一个，提供多线程的执行环境，并且通过信号量进行了控制（默认5个），防止启动太多的任务线程。
 * 
 * @author yinlei
 * @see
 * @since 2014年11月21日 下午3:48:47
 */
public class PushEventExecutor extends AbstractEventExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PushEventExecutor.class);
	
	public static final PushEventExecutor INSTANCE = new PushEventExecutor();
	
	private final Semaphore semaphore = new Semaphore(5);
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	
	private PushEventExecutor() {
		queueTask();
	}

	/**
	 * 启动执行队列任务
	 */
	private void queueTask() {
		scheduledExecutorService.schedule(new Runnable() {
			
			@Override
			public void run() {
				for (;;) {
					fetchAndExecuteTask();
				}
			}
		}, 1, TimeUnit.SECONDS);
	}
	
	/**
	 * 获取令牌授权，然后执行任务
	 */
	private void fetchAndExecuteTask() {
		if (semaphore.tryAcquire()) {
			try {
				semaphore.acquire();// 获取令牌（信号）
				Runnable task = taskQueue.take();// 此处会阻塞，直到有任务
				java.util.concurrent.Future<?> future = executorService.submit(task);
				future.get();// 执行成功，此处应该返回null
			} catch (Exception e) {
				// 异常
				LOGGER.warn("fetchAndExecuteTask执行push任务异常", e.getMessage());
			} finally {
				semaphore.release();// 释放令牌
			}
		}
	}
	
	@Override
	public EventExecutorGroup parent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean inEventLoop(Thread thread) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShuttingDown() {
		return false;
	}

	@Override
	public Future<?> shutdownGracefully(long quietPeriod, long timeout,
			TimeUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<?> terminationFuture() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return false;
	}

	@Override
	public void execute(Runnable command) {
		// 其实，可以不用在此处处理，将任务全部丢到taskQueue中，但是这样可以加快任务执行
		if (semaphore.tryAcquire()) {// 尝试获取令牌
			try {
				semaphore.acquire();
				executorService.submit(command);
			} catch (Exception e) {
				LOGGER.warn("执行push任务异常", e.getMessage());
			} finally {
				semaphore.release();
			}
		} else {
			taskQueue.offer(command);
		}
	}

	@Override
	public void shutdown() {
		scheduledExecutorService.shutdown();
		executorService.shutdown();
		taskQueue.clear();
	}

}
