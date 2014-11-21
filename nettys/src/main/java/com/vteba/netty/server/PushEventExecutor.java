package com.vteba.netty.server;

import java.util.concurrent.TimeUnit;

import io.netty.util.concurrent.AbstractEventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;

public class PushEventExecutor extends AbstractEventExecutor {

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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void execute(Runnable command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
