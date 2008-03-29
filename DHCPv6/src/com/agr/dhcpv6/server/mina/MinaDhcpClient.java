package com.agr.dhcpv6.server.mina;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoConnector;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.option.DhcpClientIdOption;
import com.agr.dhcpv6.option.DhcpElapsedTimeOption;
import com.agr.dhcpv6.option.DhcpUserClassOption;
import com.agr.dhcpv6.server.config.xml.ClientIdOption;
import com.agr.dhcpv6.server.config.xml.ElapsedTimeOption;
import com.agr.dhcpv6.server.config.xml.UserClassOption;
import com.agr.dhcpv6.util.DhcpConstants;

public class MinaDhcpClient extends IoHandlerAdapter
{
    private static Log log = LogFactory.getLog(MinaDhcpClient.class);

    private IoSession session;

    private IoConnector connector;

    /**
     * Default constructor.
     */
    public MinaDhcpClient() 
    {
        log.debug("UDPClient::UDPClient");
        log.debug("Created a datagram connector");
        connector = new NioDatagramConnector();

        log.debug("Setting the handler");
        connector.setHandler(this);

        DefaultIoFilterChainBuilder chain = connector.getFilterChain();  
        chain.addLast("logger", new LoggingFilter());

        ProtocolEncoder encoder = new DhcpEncoderAdapter();
        ProtocolDecoder decoder = new DhcpDecoderAdapter();
        chain.addLast("codec", new ProtocolCodecFilter(encoder, decoder));
        
        log.debug("About to connect to the server...");
        ConnectFuture connFuture = 
            connector.connect(new InetSocketAddress(DhcpConstants.LOCALHOST, 
                                                    10000 + DhcpConstants.SERVER_PORT));

        log.debug("About to wait.");
        connFuture.awaitUninterruptibly();

        log.debug("Adding a future listener.");
        connFuture.addListener(new IoFutureListener<ConnectFuture>() {
            public void operationComplete(ConnectFuture future) {
                if (future.isConnected()) {
                    log.debug("...connected");
                    session = future.getSession();
                    try {
                        sendData();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    log.error("Not connected...exiting");
                }
            }
        });
    }

    private void sendData() throws InterruptedException
    {
        for (int id=0; id<100; id++) {
            DhcpMessage msg = new DhcpMessage(DhcpConstants.LOCALHOST,
                                              10000 + DhcpConstants.SERVER_PORT);
            msg.setMessageType(DhcpConstants.INFO_REQUEST);
            msg.setTransactionId(id);
            byte[] clientIdBytes = { (byte)0xde,
                                     (byte)0xbd,
                                     (byte)0xeb,
                                     (byte)0xde,
                                     (byte)0xb0,
                                     (byte)id };
            ClientIdOption clientIdOption = new ClientIdOption();
            clientIdOption.setHexValue(clientIdBytes);
            
            msg.setOption(new DhcpClientIdOption(clientIdOption));
            
            ElapsedTimeOption elapsedTimeOption = new ElapsedTimeOption();
            elapsedTimeOption.setValue((short)(id+1000));
            msg.setOption(new DhcpElapsedTimeOption(elapsedTimeOption));

            UserClassOption userClassOption = new UserClassOption();
            // wrap it with the DhcpOption subclass to get at
            // utility method for adding userclass string
            DhcpUserClassOption dhcpUserClassOption = 
                new DhcpUserClassOption(userClassOption);
            dhcpUserClassOption.addUserClass("FilterUserClass");
            msg.setOption(dhcpUserClassOption);

            // write the message, the codec will convert it
            // to wire format... well, hopefully it will!
            session.write(msg);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        log.debug("Session recv...");
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.debug("Message sent...");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        log.debug("Session closed...");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.debug("Session created...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        log.debug("Session idle...");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.debug("Session opened...");
    }

    public static void main(String[] args) {
        new MinaDhcpClient();
    }

}
