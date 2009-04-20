/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TrackingThreadPool.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.net;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: TrackingThreadPool </p>
 * <p>Description: Borrowed from
 * http://www-128.ibm.com/developerworks/library/j-jtp09196 </p>
 * 
 * @author A. Gregory Rabil
 */
public class TrackingThreadPool extends ThreadPoolExecutor
{    
    /** The in progress. */
    private final Map<Runnable, Boolean> inProgress 
        = new ConcurrentHashMap<Runnable,Boolean>();
    
    /** The start time. */
    private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    
    /** The total time. */
    private long totalTime;
    
    /** The total tasks. */
    private int totalTasks;

    /**
     * Instantiates a new tracking thread pool.
     * 
     * @param corePoolSize the core pool size
     * @param maximumPoolSize the maximum pool size
     * @param keepAliveTime the keep alive time
     * @param unit the unit
     * @param workQueue the work queue
     */
    public TrackingThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
       TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread, java.lang.Runnable)
     */
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        inProgress.put(r, Boolean.TRUE);
        startTime.set(new Long(System.currentTimeMillis()));
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     */
    protected void afterExecute(Runnable r, Throwable t) {
        long time = System.currentTimeMillis() - startTime.get().longValue();
        synchronized (this) {
            totalTime += time;
            ++totalTasks;
        }
        inProgress.remove(r);
        super.afterExecute(r, t);
    }

    /**
     * Gets the in progress tasks.
     * 
     * @return the in progress tasks
     */
    public Set<Runnable> getInProgressTasks() {
        return Collections.unmodifiableSet(inProgress.keySet());
    }

    /**
     * Gets the total tasks.
     * 
     * @return the total tasks
     */
    public synchronized int getTotalTasks() {
        return totalTasks;
    }

    /**
     * Gets the average task time.
     * 
     * @return the average task time
     */
    public synchronized double getAverageTaskTime() {
        return (totalTasks == 0) ? 0 : totalTime / totalTasks;
    }
}
