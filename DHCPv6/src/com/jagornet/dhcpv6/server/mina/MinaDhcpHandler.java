package com.jagornet.dhcpv6.server.mina;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpInfoRequestProcessor;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.message.DhcpRelayMessage;
import com.jagornet.dhcpv6.option.DhcpRelayOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

public class MinaDhcpHandler extends IoHandlerAdapter
{
	private static Logger log = LoggerFactory.getLogger(MinaDhcpHandler.class);

    public MinaDhcpHandler()
    {
    	// empty constructor
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception 
    {
        log.error("Session exception caught", cause);
        session.close();
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception 
    {
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (message instanceof DhcpMessage) {
            
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            log.info("Received: " + dhcpMessage.toStringWithOptions());

            DhcpMessage replyMessage = 
            	handleMessage(((InetSocketAddress)session.getLocalAddress()).getAddress(),
            				  dhcpMessage);
            
            if (replyMessage != null) {
                // do we really want to write to the remoteAddress
                // from the session - i.e. is this really just going
                // to send the reply message to the client or relay
                // that sent or forwarded the request?
                session.write(replyMessage, remoteAddress);
            }
            else {
                log.warn("Null DHCP reply message returned from processor");
            }
            
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
    
    public DhcpMessage handleMessage(InetAddress localAddress,
    								 DhcpMessage dhcpMessage)
    {
		DhcpMessage replyMessage = null;
		if (dhcpMessage.getMessageType() == DhcpConstants.INFO_REQUEST) {
		    replyMessage = handleInfoRequest(localAddress, dhcpMessage);
		}
		else if (dhcpMessage.getMessageType() == DhcpConstants.RELAY_FORW) {
		    if (dhcpMessage instanceof DhcpRelayMessage) {
		        DhcpRelayMessage relayMessage = (DhcpRelayMessage) dhcpMessage;
		        replyMessage = handleRelayFoward(relayMessage);
		    }
		    else {
		        // Note: in theory, we can't get here, because the
		        // codec would have thrown an exception beforehand
		        log.error("Received unknown relay message object: " + 
		                  dhcpMessage.getClass());
		    }
		}
		else {
		    log.warn("Ignoring unsupported message type: " + 
		             DhcpConstants.getMessageString(dhcpMessage.getMessageType()));
		}
		return replyMessage;
	}
    
    private DhcpMessage handleInfoRequest(InetAddress linkAddr, 
                                          DhcpMessage dhcpMessage)
    {
        DhcpInfoRequestProcessor processor = 
            new DhcpInfoRequestProcessor(linkAddr, dhcpMessage);
        
        DhcpMessage reply = processor.process();
        return reply;
    }
    
    private DhcpRelayMessage handleRelayFoward(DhcpRelayMessage relayMessage)
    {
    	/**
    	 * TODO: Verify that because we re-use the relay_forward message
    	 * 		 for our relay_reply, then we will end up including any
    	 * 		 Interface-ID option that was contained therein, as
    	 * 		 required by RFC 3315 Section 22.18.
    	 */
        InetAddress linkAddr = relayMessage.getLinkAddress();
        DhcpRelayOption relayOption = relayMessage.getRelayOption();
        if (relayOption != null) {
            DhcpMessage relayOptionMessage = relayOption.getRelayMessage();
            while (relayOptionMessage != null) {
                // check what kind of message is in the option
                if (relayOptionMessage instanceof DhcpRelayMessage) {
                    // encapsulated message is another relay message
                    DhcpRelayMessage anotherRelayMessage = 
                        (DhcpRelayMessage)relayOptionMessage;
                    // flip this inner relay_forward into a relay_reply,
                    // because we reuse the relay message "stack" for the reply
                    anotherRelayMessage.setMessageType(DhcpConstants.RELAY_REPL);
                    // reset the client link reference
                    linkAddr = anotherRelayMessage.getLinkAddress();
                    // reset the current relay option reference to the
                    // encapsulated relay message's relay option
                    relayOption = anotherRelayMessage.getRelayOption();
                    // reset the relayOptionMessage reference to recurse
                    relayOptionMessage = relayOption.getRelayMessage();
                }
                else {
                    // we've peeled off all the layers of the relay message(s),
                    // so now go handle the Info-Request, assuming it is one
                    if (relayOptionMessage.getMessageType() == DhcpConstants.INFO_REQUEST) {
                        DhcpMessage replyMessage = 
                            handleInfoRequest(linkAddr, relayOptionMessage);
                        if (replyMessage != null) {
                            // replace the original Info-Request message inside
                            // the relayed message with the generated Reply message
                            relayOption.setRelayMessage(replyMessage);
                            // flip the outer-most relay_foward into a relay_reply
                            relayMessage.setMessageType(DhcpConstants.RELAY_REPL);
                            // return the relay message we started with, 
                            // with each relay "layer" flipped from a relay_forward
                            // to a relay_reply, and the lowest level relayOption
                            // will contain our Reply for the Info-Request
                            return relayMessage;
                        }
                    }
                    else {
                        log.error("Lowest level message in relay message is not an Info-Request");
                    }
                    relayOptionMessage = null;  // done with relayed messages
                }
            }
        }
        else {
            log.error("Relay message does not contain a relay option");
        }
        // if we get here, no reply was generated
        return null;
    }
}
