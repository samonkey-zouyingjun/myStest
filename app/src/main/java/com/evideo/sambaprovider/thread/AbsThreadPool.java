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

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbsThreadPool{
    private static ArrayList<AbsThreadPool> mPools = new ArrayList<AbsThreadPool>();
    {
    	mPools.add(this);
    }
    protected BlockingQueue<Runnable> queue;
    protected ThreadPoolExecutor executor;
    private boolean isDestroy = false;
    private IClearOutmodedTaskSetting mClearOutmodedTaskSetting;
    
    protected abstract int getCorePoolSize();
    
    protected abstract int getMaximumPoolSize();
    
    protected abstract long getKeepAliveTime();
    
    protected abstract TimeUnit getTimeUnit();
    
    protected abstract BlockingQueue<Runnable> newQueue();
    
    protected IClearOutmodedTaskSetting newClearOutmodedTaskSetting(){
		return null;
    };
    
    protected ThreadFactory newThreadFactory(){
		return new DefaultThreadFactory();
    }
    
    private boolean checkInit(){
    	if(isDestroy){
    		return false;
    	}
    	if(executor == null){
    		queue = newQueue();
    	}
    	if(executor == null || executor.isShutdown()){
    		mClearOutmodedTaskSetting = newClearOutmodedTaskSetting();
    		executor = new ThreadPoolExecutor(
        			getCorePoolSize(),
        			getMaximumPoolSize(),
        			getKeepAliveTime(),
        			getTimeUnit(),
        			queue, newThreadFactory());
    	}
    	return true;
    }
    
    /**
    * 增加新的任务
    * 每增加一个新任务，都要唤醒任务队列
    * @param newTask
    */
    public final void addTask(Runnable newTask) {
    	if(newTask == null){
    		return;
    	}
    	newTask = onAddTask(newTask);
    	if(newTask == null){
    		return;
    	}
    	synchronized (this) {
    		if(checkInit()){
    			clearOutmodedTask();
        		executor.execute(newTask);
        	}
		}
    }
    
    protected void clearOutmodedTask(){
    	if(mClearOutmodedTaskSetting == null){
    		return;
    	}
    	BlockingQueue<Runnable> queue = executor.getQueue();
    	for(;true;){
    		if(mClearOutmodedTaskSetting.isNeedPoll(queue)){
				queue.poll();
			}else{
				break;
			}
		}
    }
    
    /**
     * 可通过重写此方法对添加的任务进行装饰
     * @param newTask
     * @return
     */
    protected Runnable onAddTask(Runnable newTask){
		return newTask;
    }
    
    protected boolean isNeedAutoRelease(){
    	return true;
    }
    
    public final void releaseRes(){
    	synchronized (this) {
	    	if(executor != null && !executor.isShutdown() ){
	    		executor.shutdown();
	    		if(queue != null) {
	    			queue.clear();
	    			queue =  null;
	    		}
	    		executor = null;
	    		mClearOutmodedTaskSetting = null;
	    	}
    	}
    }
    
    protected void onDestroy(){
    	
    }
    
    /**
    * 销毁线程池
    */
    public final synchronized void destroy() {
    	synchronized (this) {
	    	if(!isDestroy){
	    		releaseRes();
	    		isDestroy = true;
	    		onDestroy();
	    		mPools.remove(this);
	    	}
    	}
    }
    
    public static synchronized void releaseAllPools(){
    	for(AbsThreadPool pool: mPools){
    		if(pool.isNeedAutoRelease()){
        		pool.releaseRes();
    		}
    	}
    }
    
    public static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "AbsThreadPool - " +
                          poolNumber.getAndIncrement() +
                         " - thread - ";
        }

        @Override
		public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
    
    public interface IClearOutmodedTaskSetting{
    	public abstract boolean isNeedPoll(BlockingQueue<Runnable> queue);
    }
}
