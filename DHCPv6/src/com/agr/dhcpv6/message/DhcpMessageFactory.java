package com.agr.dhcpv6.message;

import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.util.DhcpConstants;

public class DhcpMessageFactory
{
    private static Log log = LogFactory.getLog(DhcpMessageFactory.class);

    public static DhcpMessage getDhcpMessage(byte msgtype, InetAddress from) 
    {
        DhcpMessage dhcpMessage = null;
        switch (msgtype) {
            
            case DhcpConstants.INFO_REQUEST:
                dhcpMessage = new DhcpMessage(from);
                break;
                
            case DhcpConstants.RELAY_FORW:
                dhcpMessage = new DhcpRelayMessage(from);
                break;

            case DhcpConstants.CONFIRM:
            case DhcpConstants.DECLINE:
            case DhcpConstants.REBIND:
            case DhcpConstants.RELEASE:
            case DhcpConstants.RENEW:
            case DhcpConstants.REQUEST:
            case DhcpConstants.SOLICIT:
                log.warn("Unsupported message type.");
                break;
                
            case DhcpConstants.ADVERTISE:
            case DhcpConstants.RECONIFURE:
            case DhcpConstants.RELAY_REPL:
            case DhcpConstants.REPLY:
                log.warn("Server message received.");
                // since this factory is used by the test client via
                // the decoder, we want to return a message for now
                dhcpMessage = new DhcpMessage(from);
                break;
                
            default:
                log.error("Unknown message type.");
                break;
        }
        return dhcpMessage;
    }
}
