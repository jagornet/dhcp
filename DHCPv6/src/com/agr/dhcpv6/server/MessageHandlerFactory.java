package com.agr.dhcpv6.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.message.DhcpMessageHandler;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * <p>Title: MessageHandlerFactory </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class MessageHandlerFactory
{
	private static Logger log = LoggerFactory.getLogger(MessageHandlerFactory.class);

    // private constructor - can't instantiate this class
    private MessageHandlerFactory() { }
    
    public static DhcpMessageHandler createMessageHandler(DhcpChannel channel, DhcpMessage message)
    {
        DhcpMessageHandler handler = null;
        short msgType = message.getMessageType();
        if (msgType == DhcpConstants.INFO_REQUEST) {
            handler = new DhcpInfoRequestHandler(channel, message);
        }
        else {
            log.error("No message handler for: " + 
                      DhcpConstants.getMessageString(msgType));
        }
        return handler;
    }
}
