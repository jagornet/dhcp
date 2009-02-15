package com.jagornet.dhcpv6.server.channel;

/**
 * <p>Title: ThreadPoolStatusMBean </p>
 * <p>Description: Borrowed from 
 * http://www-128.ibm.com/developerworks/library/j-jtp09196 </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public interface ThreadPoolStatusMBean
{
    public int getActiveThreads();
    public int getActiveTasks();
    public int getTotalTasks();
    public int getQueuedTasks();
    public double getAverageTaskTime();
    public String[] getActiveTaskNames();
    public String[] getQueuedTaskNames();
}