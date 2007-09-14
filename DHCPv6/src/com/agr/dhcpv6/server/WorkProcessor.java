package com.agr.dhcpv6.server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Title: WorkProcessor </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class WorkProcessor implements Runnable
{
    private static Log log = LogFactory.getLog(WorkProcessor.class);

    private static int POOL_SIZE = 100;  

    private DhcpChannel dhcpChannel;
    private BlockingQueue<DhcpMessage> workQueue;
//    private ExecutorService threadPool;
    private TrackingThreadPool threadPool;

    public WorkProcessor(DhcpChannel dhcpChannel, BlockingQueue<DhcpMessage> workQueue)
    {
        this.dhcpChannel = dhcpChannel;
        this.workQueue = workQueue;
//        threadPool = Executors.newFixedThreadPool(POOL_SIZE);
        threadPool = new TrackingThreadPool(POOL_SIZE, 
                                            POOL_SIZE,
                                            0,
                                            TimeUnit.SECONDS,
                                            new LinkedBlockingQueue<Runnable>());
        ThreadPoolStatus threadPoolStatus = new ThreadPoolStatus(threadPool);
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            log.debug("Registering ThreadPoolStatus MBean...");
            server.registerMBean(threadPoolStatus, 
                                 new ObjectName("dhcpv6:type=WorkProcessor,name=ThreadPool"));
            log.debug("ThreadPoolStatus MBean registered successfully.");
        }
        catch (Exception ex) {
            log.error("Failed to register ThreadPoolStatus MBean: " + ex);
        }        
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        while (true) {
            try {
                log.info("Waiting for work...");
                DhcpMessage message = workQueue.take();
                if (message != null) {
                    DhcpMessageHandler handler =
                        MessageHandlerFactory.createMessageHandler(dhcpChannel, message);
                    if (log.isDebugEnabled())
                        log.debug("Sending to pool: " + message.toStringWithOptions());
                    else if (log.isInfoEnabled())
                        log.info("Sending to pool: " + message.toString());
                    threadPool.execute(handler);
                }
            }
            catch (InterruptedException ex) {
                log.info("Interrupted while waiting");
            }
        }
    }

}
