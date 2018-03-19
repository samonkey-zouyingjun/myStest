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
import java.util.concurrent.TimeUnit;

/**
 * [功能说明]使用lifo算法的线程池
 */
public class LifoTerminableThreadPool extends AbsTerminableThread {
    
    /** [核心线程数量] */
    private static final int CORE_POOL_SIZE = 2;
    
    /** [线程池中线程的最大允许数量] */
    private static final int MAXIMUM_POOL_SIZE = 20;
    
    /** [线程数超过核心线程数量时,会回收超过此参数定义的时间的空闲线程,单位秒] */
    private static final int KEEP_ALIVE_TIME = 60;
    
    private LifoThreadPool mLifoThreadPool;
    
    public LifoTerminableThreadPool() {
        this(null);
    }
    
    public LifoTerminableThreadPool(Runnable task) {
        super(task);
        mLifoThreadPool = LifoThreadPool.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runTask(Runnable runnable) {
        mLifoThreadPool.addTask(runnable);
    }
    
    /**
     * [功能说明]使用lifo算法的线程池
     * 单例
     */
    private static final class LifoThreadPool extends AbsThreadPool {
        
        private static LifoThreadPool sInstance;
        
        private LifoThreadPool() {
        }
        
        public static LifoThreadPool getInstance() {
            if (sInstance == null) {
                synchronized (LifoThreadPool.class) {
                    if (sInstance == null) {
                        sInstance = new LifoThreadPool();
                    }
                }
            }
            return sInstance;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected int getMaximumPoolSize() {
            return MAXIMUM_POOL_SIZE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected long getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected TimeUnit getTimeUnit() {
            return TimeUnit.SECONDS;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected BlockingQueue<Runnable> newQueue() {
            return new LifoLinkedBlockingDeque<Runnable>();
        }
        
    }

}
