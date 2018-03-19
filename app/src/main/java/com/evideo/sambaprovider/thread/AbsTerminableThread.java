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



public abstract class AbsTerminableThread implements ITerminableThread{
	@SuppressWarnings("unused")
    private static final String TAG = "AsyncTaskManage";
	
	private AsyncTaskManage mAsyncTaskManage;
	private AsyncTaskManage.ThreadInfo mThreadInfo;
	private Runnable mTask;
	private boolean isStarted = false;
	private boolean isCancel = true;
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			@SuppressWarnings("unused")
            Thread currentThread = Thread.currentThread();
//			EvLog.i(TAG, "InterruptibleThread run() -->开始:isCancel="+isCancel+";tId="+currentThread.getId()+";tHCode="+currentThread.hashCode());
			if(isCancel){
				return;
			}
			mThreadInfo = mAsyncTaskManage.registerThread();
			if(mTask != null){
				mTask.run();
			}
			AbsTerminableThread.this.run();
			mThreadInfo = null;
			mAsyncTaskManage.unregisterThread();
			isCancel = true;
		}
	};
	
	public AbsTerminableThread(){
		this(null);
	}
	
	public AbsTerminableThread(Runnable task){
		mAsyncTaskManage = AsyncTaskManage.getInstance();
		mTask = task;
	}
	
	public void setTask(Runnable task){
		mTask = task;
	}
	
	public void run(){
		
	}
	
	@Override
	public final void start(){
		if(isStarted){
			return;
		}
		isStarted = true;
		isCancel = false;
		runTask(mRunnable);
	}
	
	protected abstract void runTask(Runnable runnable);
	
	@Override
	public final void cancel(){
		isCancel = true;
		if(mThreadInfo != null){
			mAsyncTaskManage.cancelAsyncTask(mThreadInfo);
		}
	}

	@Override
	public boolean isCancel() {
		return isCancel;
	}
	
}
