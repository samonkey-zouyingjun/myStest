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


import com.evideo.sambaprovider.util.Logs;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @brief : [异步任务管理类，可以控制异步任务快速结束]
 */
public final class AsyncTaskManage {
    private static final String TAG = "AsyncTaskManage";
    private static AsyncTaskManage sIntance;
    /**
     * 线程不支持异步任务管理
     */
    public static final int RESULT_NONSUPPORT = 0;
    /**
     * 注册任务成功
     */
    public static final int RESULT_SUCCEED = 1;
    /**
     * 线程已经停止
     */
    public static final int RESULT_STOP = 2;
    /***/
    public static final boolean DEBUG = false;
    private ConcurrentHashMap<Long, IAsyncTask> mAsyncTaskMap;
    private ConcurrentHashMap<Long, ThreadInfo> mThreadInfoMap;
    private ConcurrentHashMap<Long, byte[]> mLockMap;
    /**
     * [功能说明]
     * @return 实例化
     */
    public static synchronized  AsyncTaskManage getInstance() {
        if (sIntance == null) {
            sIntance = new AsyncTaskManage();
        }
        return sIntance;
    }

    private AsyncTaskManage() {
        mAsyncTaskMap = new ConcurrentHashMap<Long, IAsyncTask>();
        mThreadInfoMap = new ConcurrentHashMap<Long, ThreadInfo>();
        mLockMap = new ConcurrentHashMap<Long, byte[]>();
    }

    /**
     * 异步任务管理生命周期在此方法调用后开始，确保这个方法在线程要执行的任务之前调用。
     * @return 线程信息
     */
    public ThreadInfo registerThread() {
        Thread currentThread = Thread.currentThread();
        if (DEBUG)
            Logs.i(TAG, "registerThread -->开始:tId=" + currentThread.getId()
                    + ";tHCode=" + currentThread.hashCode());
        synchronized (lock(currentThread.getId())) {
            try {
                ThreadInfo threadInfo = mThreadInfoMap.get(currentThread
                        .getId());
                if (threadInfo == null
                        || threadInfo.getHashCode() != currentThread.hashCode()) {
                    threadInfo = new ThreadInfo(currentThread.getId(),
                            currentThread.hashCode());
                    mThreadInfoMap.put(currentThread.getId(), threadInfo);
                }
                threadInfo.setRegister(true);
                return ThreadInfo.copy(threadInfo);
            } finally {
                unlock(currentThread.getId());
                if (DEBUG)
                    Logs.i(TAG,
                            "registerThread -->结束:tId=" + currentThread.getId()
                                    + ";tHCode=" + currentThread.hashCode());
            }
        }
    }

    /**
     * 异步任务管理生命周期在此方法调用后结束，确保这个方法在线程要执行的任务之后调用。
     */
    public void unregisterThread() {
        Thread currentThread = Thread.currentThread();
        if (DEBUG)
            Logs.i(TAG, "unregisterThread -->开始:tId=" + currentThread.getId()
                    + ";tHCode=" + currentThread.hashCode());
        synchronized (lock(currentThread.getId())) {
            try {
                mThreadInfoMap.remove(currentThread.getId());
                mAsyncTaskMap.remove(currentThread.getId());
            } finally {
                unlock(currentThread.getId());
                if (DEBUG)
                    Logs.i(TAG,
                            "unregisterThread -->结束:tId="
                                    + currentThread.getId() + ";tHCode="
                                    + currentThread.hashCode());
            }
        }
    }

    /**
     * 注册任务
     * 
     * @param httpTask
     * @return 返回值是{@link #RESULT_NONSUPPORT}、{@link #RESULT_STOP}、
     *         {@link #RESULT_SUCCEED}
     */
    public int registerHttpTask(IAsyncTask httpTask) {
        Thread currentThread = Thread.currentThread();
        if (DEBUG)
            Logs.i(TAG, "registerHttpTask -->开始:tId=" + currentThread.getId()
                    + ";tHCode=" + currentThread.hashCode());
        int result = RESULT_NONSUPPORT;
        synchronized (lock(currentThread.getId())) {
            try {
                ThreadInfo threadInfo = mThreadInfoMap.get(currentThread
                        .getId());
                if (threadInfo != null
                        && threadInfo.getHashCode() == currentThread.hashCode()) {
                    if (threadInfo.getState() == ThreadInfo.STATE_STOP) {
                        result = RESULT_STOP;
                        return result;
                    } else {
                        mAsyncTaskMap.put(currentThread.getId(), httpTask);
                        result = RESULT_SUCCEED;
                        return result;
                    }
                }
                return result;
            } finally {
                unlock(currentThread.getId());
                if (DEBUG)
                    Logs.i(TAG, "registerHttpTask -->结束:result=" + result
                            + ";tId=" + currentThread.getId() + ";tHCode="
                            + currentThread.hashCode());
            }
        }
    }

    public void cancelAsyncTask(ThreadInfo threadInfo) {
        if (threadInfo == null) {
            return;
        }
        cancelAsyncTask(threadInfo.getThreadId(), threadInfo.getHashCode());
    }

    public void cancelAsyncTask(final long threadId, final int threadHashCode) {
        if (DEBUG)
            Logs.i(TAG, "cancelAsyncTask -->开始:tId=" + threadId + ";tHCode="
                    + threadHashCode);
        synchronized (lock(threadId)) {
            try {
                ThreadInfo threadInfo = mThreadInfoMap.get(threadId);
                if (threadInfo != null) {
                    if (threadInfo.isRegister() && !threadInfo.isCancel()) {
                        IAsyncTask asyncTask = mAsyncTaskMap.get(threadId);
                        if (asyncTask != null) {
                            Logs.i(TAG, "cancelAsyncTask -->执行onCancel():tId="
                                    + threadId + ";tHCode=" + threadHashCode);
                            asyncTask.onCancel();
                            threadInfo.setCancel(true);
                        }
                    }
                    threadInfo.setState(ThreadInfo.STATE_STOP);
                }
            } finally {
                unlock(threadId);
                if (DEBUG)
                    Logs.i(TAG, "cancelAsyncTask -->结束:tId=" + threadId
                            + ";tHCode=" + threadHashCode);
            }
        }
    }

    private synchronized byte[] lock(long threadId) {
        byte[] lock = mLockMap.get(threadId);
        if (lock == null) {
            lock = new byte[0];
            mLockMap.put(threadId, lock);
        }
        return lock;
    }

    private synchronized void unlock(long threadId) {
        mLockMap.remove(threadId);
    }

    public static interface IAsyncTask {
        public void onCancel();
    }

    public static class ThreadInfo {
        public static final int STATE_RUNING = 0;
        public static final int STATE_STOP = 1;
        private long mThreadId;
        private int mHashCode;
        private int mState;
        private boolean isRegister;
        private boolean isCancel;

        public ThreadInfo(long threadId, int hashCode) {
            setHashCode(hashCode);
            setThreadId(threadId);
            setState(STATE_RUNING);
            setRegister(false);
            setCancel(false);
        }

        /**
         * @return the isCancel
         */
        public boolean isCancel() {
            return isCancel;
        }

        /**
         * @param isCancel
         *            the isCancel to set
         */
        public void setCancel(boolean isCancel) {
            this.isCancel = isCancel;
        }

        /**
         * @return the isRegister
         */
        private boolean isRegister() {
            return isRegister;
        }

        /**
         * @param isRegister
         *            the isRegister to set
         */
        private void setRegister(boolean isRegister) {
            this.isRegister = isRegister;
        }

        /**
         * @return the threadId
         */
        public long getThreadId() {
            return mThreadId;
        }

        /**
         * @param threadId
         *            the threadId to set
         */
        public void setThreadId(long threadId) {
            this.mThreadId = threadId;
        }

        /**
         * @return the hashCode
         */
        public int getHashCode() {
            return mHashCode;
        }

        /**
         * @param hashCode
         *            the hashCode to set
         */
        public void setHashCode(int hashCode) {
            this.mHashCode = hashCode;
        }

        /**
         * @return the state
         */
        private int getState() {
            return mState;
        }

        /**
         * @param state
         *            the state to set
         */
        private void setState(int state) {
            this.mState = state;
        }

        public static ThreadInfo copy(ThreadInfo threadInfo) {
            if (threadInfo != null) {
                threadInfo = new ThreadInfo(threadInfo.getThreadId(),
                        threadInfo.getHashCode());
            }
            return threadInfo;
        }
    }
}
