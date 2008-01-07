package com.agr.dhcpv6.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static Log log = LogFactory.getLog(MessageHandlerFactory.class);

    // private constructor - can't instantiate this class
    private MessageHandlerFactory() { }
    
    public static DhcpMessageHandler createMessageHandler(DhcpChannel channel, DhcpMessage message)
    {
        DhcpMessageHandler handler = null;
        byte msgType = message.getMessageType();
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
