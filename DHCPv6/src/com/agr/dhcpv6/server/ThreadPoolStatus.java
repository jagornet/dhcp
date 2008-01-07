package com.agr.dhcpv6.server;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>Title: ThreadPoolStatus </p>
 * <p>Description: Borrowed from 
 * http://www-128.ibm.com/developerworks/library/j-jtp09196 </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class ThreadPoolStatus implements ThreadPoolStatusMBean {
    private final TrackingThreadPool pool;

    public ThreadPoolStatus(TrackingThreadPool pool) {
        this.pool = pool;
    }

    public int getActiveThreads() {
        return pool.getPoolSize();
    }

    public int getActiveTasks() {
        return pool.getActiveCount();
    }

    public int getTotalTasks() {
        return pool.getTotalTasks();
    }

    public int getQueuedTasks() {
        return pool.getQueue().size();
    }

    public double getAverageTaskTime() {
        return pool.getAverageTaskTime();
    }

    public String[] getActiveTaskNames() {
        return toStringArray(pool.getInProgressTasks());
    }

    public String[] getQueuedTaskNames() {
        return toStringArray(pool.getQueue());
    }

    private String[] toStringArray(Collection<Runnable> collection) {
        ArrayList<String> list = new ArrayList<String>();
        for (Runnable r : collection)
            list.add(r.toString());
        return list.toArray(new String[0]);
    }
}