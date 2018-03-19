/*
 * Copyright 2018 Google Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.evideo.sambaprovider.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TerminableThreadPool extends AbsTerminableThread {
	
	private ThreadPool mThreadPool;
	public TerminableThreadPool(){
		this(null);
	}
	public TerminableThreadPool(Runnable task){
		super(task);
		mThreadPool = ThreadPool.getInstance();
	}
	
	@Override
	protected void runTask(Runnable runnable) {
		mThreadPool.addTask(runnable);
	}
	
	public static void releaseRes(){
		ThreadPool.getInstance().releaseRes();
	}
	
	private static class ThreadPool extends AbsThreadPool{
	    /* 单例 */
	    private static ThreadPool instance = new ThreadPool();

	    public static synchronized ThreadPool getInstance() {
	        if (instance == null){
	        	instance = new ThreadPool();
	        }
	        return instance;
	    }
	    
	    private ThreadPool(){
	    	
	    }

		@Override
		protected int getCorePoolSize() {
			return 20;
		}

		@Override
		protected int getMaximumPoolSize() {
			return 100;
		}

		@Override
		protected long getKeepAliveTime() {
			return 60;
		}

		@Override
		protected TimeUnit getTimeUnit() {
			return TimeUnit.SECONDS;
		}

		@Override
		protected BlockingQueue<Runnable> newQueue() {
			return new LinkedBlockingQueue<Runnable>();
		}
	}
}
