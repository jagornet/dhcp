/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ThreadPoolStatus.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.multicast;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>Title: ThreadPoolStatus </p>
 * <p>Description: Borrowed from
 * http://www-128.ibm.com/developerworks/library/j-jtp09196 </p>
 * 
 * @author A. Gregory Rabil
 */
public class ThreadPoolStatus implements ThreadPoolStatusMBean
{
    /** The pool. */
    private final TrackingThreadPool pool;

    /**
     * Instantiates a new thread pool status.
     * 
     * @param pool the pool
     */
    public ThreadPoolStatus(TrackingThreadPool pool) {
        this.pool = pool;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.multicast.ThreadPoolStatusMBean#getActiveThreads()
     */
    public int getActiveThreads() {
        return pool.getPoolSize();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.multicast.ThreadPoolStatusMBean#getActiveTasks()
     */
    public int getActiveTasks() {
        return pool.getActiveCount();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.multicast.ThreadPoolStatusMBean#getTotalTasks()
     */
    public int getTotalTasks() {
        return pool.getTotalTasks();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.multicast.ThreadPoolStatusMBean#getQueuedTasks()
     */
    public int getQueuedTasks() {
        return pool.getQueue().size();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.multicast.ThreadPoolStatusMBean#getAverageTaskTime()
     */
    public double getAverageTaskTime() {
        return pool.getAverageTaskTime();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.multicast.ThreadPoolStatusMBean#getActiveTaskNames()
     */
    public String[] getActiveTaskNames() {
        return toStringArray(pool.getInProgressTasks());
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.multicast.ThreadPoolStatusMBean#getQueuedTaskNames()
     */
    public String[] getQueuedTaskNames() {
        return toStringArray(pool.getQueue());
    }

    /**
     * To string array.
     * 
     * @param collection the collection
     * 
     * @return the string[]
     */
    private String[] toStringArray(Collection<Runnable> collection) {
        ArrayList<String> list = new ArrayList<String>();
        for (Runnable r : collection)
            list.add(r.toString());
        return list.toArray(new String[0]);
    }
}
