package com.agr.dhcpv6.server.mina;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.integration.jmx.IoServiceMBean;
import org.apache.mina.integration.jmx.IoSessionMBean;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.server.config.DhcpServerConfiguration;
import com.agr.dhcpv6.util.DhcpConstants;

public class MinaDhcpServer
{
    private static Log log = LogFactory.getLog(MinaDhcpServer.class);
    
    protected NioDatagramAcceptor acceptor;
    protected ExecutorService executorService;
    
    public MinaDhcpServer() 
    {
        try {
            DhcpServerConfiguration.init("/svn/dhcpv6/trunk/DHCPv6/src/com/agr/dhcpv6/server/dhcpServerConfig2.xml");
            
            acceptor = new NioDatagramAcceptor();
            acceptor.setLocalAddress(new InetSocketAddress(DhcpConstants.SERVER_PORT));
            acceptor.setHandler(new MinaDhcpHandler(this));
    
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
 *          For an application with a ProtocolCodecFilter implemetation and 
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

            log.info("Binding to: " + acceptor.getLocalAddress());
            acceptor.bind();
        }
        catch (Exception ex) {
            log.error("Failed to start server: " + ex);
        }
    }
    
    // TODO: Support calling this shutdown method somehow
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
            ObjectName name = new ObjectName( "com.agr.dhcpv6:type=IOServiceMBean,name=MinaDhcpV6Server" );
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
                            new ObjectName("com.agr.dhcpv6:type=IoSessionMBean,name=" + 
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
                            new ObjectName("com.agr.dhcpv6:type=IoSessionMBean,name=" + 
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
    
    protected void recvUpdate(SocketAddress clientAddr, DhcpMessage dhcpMessage)
    {
        log.info("Received: " + dhcpMessage.toStringWithOptions());
    }

    public static void main(String[] args) throws IOException
    {
        new MinaDhcpServer();
    }
}
