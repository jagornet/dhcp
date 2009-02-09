package com.jagornet.dhcpv6.server.mina;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.LogManager;
import org.apache.log4j.jmx.HierarchyDynamicMBean;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.integration.jmx.IoServiceMBean;
import org.apache.mina.integration.jmx.IoSessionMBean;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;

public class MinaDhcpServer
{
	private static Logger log = LoggerFactory.getLogger(MinaDhcpServer.class);
    
    protected NioDatagramAcceptor acceptor;
    protected ExecutorService executorService;
    
    public MinaDhcpServer(String configFilename, int port) throws Exception
    {
        try {
            DhcpServerConfiguration.init(configFilename);
            
            acceptor = new NioDatagramAcceptor();
            List<SocketAddress> localAddrs = new ArrayList<SocketAddress>();
            localAddrs.add(new InetSocketAddress(port));
// We can't yet support Multicast addresses with MINA, and if/when we do
// this may not be the way to specify these addresses anyway
//            localAddrs.add(new InetSocketAddress(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS,
//                                                 port));
//            localAddrs.add(new InetSocketAddress(DhcpConstants.ALL_DHCP_SERVERS,
//                                                 port));
            acceptor.setDefaultLocalAddresses(localAddrs);
            acceptor.setHandler(new MinaDhcpHandler());
    
            registerJmx(acceptor);
            
            DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();  
            chain.addLast("logger", new LoggingFilter());
            
            ProtocolEncoder encoder = new DhcpEncoderAdapter();
            ProtocolDecoder decoder = new DhcpDecoderAdapter();
/*
 * From the wiki documentation:
 * 
 *      Where should I put an ExecutorFilter in an IoFilterChain?
 * 
 *          It depends on the characteristics of your application. 
 *          For an application with a ProtocolCodecFilter implementation and 
 *          a usual IoHandler implementation with database operations, 
 *          I'd suggest you to add the ExecutorFilter after the 
 *          ProtocolCodecFilter implementation. It is because the 
 *          performance characteristic of most protocol codec implementations 
 *          is CPU-bound, which is the same with I/O processor threads.      
 */        
            // Add CPU-bound job first,
            chain.addLast("codec", new ProtocolCodecFilter(encoder, decoder));
    
            // and then a thread pool.
            executorService = Executors.newCachedThreadPool();
            chain.addLast("threadPool", new ExecutorFilter(executorService));    
            
            DatagramSessionConfig dcfg = acceptor.getSessionConfig();
            dcfg.setReuseAddress(true);

            registerLog4jInJmx();

        }
        catch (Exception ex) {
            log.error("Failed to initialize server: " + ex, ex);
            throw ex;
        }
    }
    
    public void start() throws Exception
    {        
        List<SocketAddress> defaultAddrs = acceptor.getDefaultLocalAddresses();
        for (SocketAddress socketAddress : defaultAddrs) {
            log.info("Binding to local address: " + socketAddress);
        }
        acceptor.bind();
    }
    
    // TODO: Support calling this shutdown method somehow
    //       see - http://java.sun.com/j2se/1.4.2/docs/guide/lang/hook-design.html
    protected void shutdown()
    {
        acceptor.unbind();
        executorService.shutdown();        
    }
    
    protected void registerJmx(IoService service)
    {
        try {
            IoServiceMBean serviceMBean = new IoServiceMBean(service);
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();  
            ObjectName name = new ObjectName( "com.jagornet.dhcpv6:type=IOServiceMBean,name=MinaDhcpV6Server" );
            mbs.registerMBean(serviceMBean, name);
            service.addListener( new IoServiceListener()
            {
                public void serviceActivated(IoService service) {
                }

                public void serviceDeactivated(IoService service) {
                }

                public void serviceIdle(IoService service, IdleStatus idleStatus) {
                }

                public void sessionCreated(IoSession session)
                {
                    try {
                        IoSessionMBean sessionMBean = new IoSessionMBean(session);
                        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();  
                        ObjectName name = 
                            new ObjectName("com.jagornet.dhcpv6:type=IoSessionMBean,name=" + 
                                           session.getRemoteAddress().toString().replace( ':', '/' ) );
                        mbs.registerMBean(sessionMBean, name);
                    }
                    catch(JMException ex) {
                        log.error("JMX Exception in sessionCreated: ", ex);
                    }      
                }

                public void sessionDestroyed(IoSession session)
                {
                    try {
                        ObjectName name = 
                            new ObjectName("com.jagornet.dhcpv6:type=IoSessionMBean,name=" + 
                                           session.getRemoteAddress().toString().replace( ':', '/' ) );
                        ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
                    }
                    catch(JMException ex) {
                        log.error("JMX Exception in sessionDestroyed: ", ex);
                    }      
                }
            });

        }
        catch (Exception ex) {
            log.error("Failure registering server in JMX: " + ex);
        }
    }
    
    protected void registerLog4jInJmx()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();  
        try {
            // Create and Register the top level Log4J MBean
            HierarchyDynamicMBean hdm = new HierarchyDynamicMBean();
            ObjectName mbo = new ObjectName("log4j:hiearchy=default");
            mbs.registerMBean(hdm, mbo);
    
            // Add the root logger to the Hierarchy MBean
            org.apache.log4j.Logger rootLogger =
            	org.apache.log4j.Logger.getRootLogger();
            hdm.addLoggerMBean(rootLogger.getName());
    
            // Get each logger from the Log4J Repository and add it to
            // the Hierarchy MBean created above.
            LoggerRepository r = LogManager.getLoggerRepository();
            Enumeration<Logger> loggers = r.getCurrentLoggers();
            if (loggers != null) {
                while (loggers.hasMoreElements()) {
                	org.apache.log4j.Logger logger = 
                		(org.apache.log4j.Logger) loggers.nextElement();
                    hdm.addLoggerMBean(logger.getName());
                }
            }
        }
        catch (Exception ex) {
            log.error("Failure registering Log4J in JMX: " + ex);
        }
    }
}
