/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ThreadPoolStatusMBean.java is part of DHCPv6.
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

/**
 * <p>Title: ThreadPoolStatusMBean </p>
 * <p>Description: Borrowed from
 * http://www-128.ibm.com/developerworks/library/j-jtp09196 </p>
 * 
 * @author A. Gregory Rabil
 */
public interface ThreadPoolStatusMBean
{
    /**
     * Gets the active threads.
     * 
     * @return the active threads
     */
    public int getActiveThreads();
    
    /**
     * Gets the active tasks.
     * 
     * @return the active tasks
     */
    public int getActiveTasks();
    
    /**
     * Gets the total tasks.
     * 
     * @return the total tasks
     */
    public int getTotalTasks();
    
    /**
     * Gets the queued tasks.
     * 
     * @return the queued tasks
     */
    public int getQueuedTasks();
    
    /**
     * Gets the average task time.
     * 
     * @return the average task time
     */
    public double getAverageTaskTime();
    
    /**
     * Gets the active task names.
     * 
     * @return the active task names
     */
    public String[] getActiveTaskNames();
    
    /**
     * Gets the queued task names.
     * 
     * @return the queued task names
     */
    public String[] getQueuedTaskNames();
}
