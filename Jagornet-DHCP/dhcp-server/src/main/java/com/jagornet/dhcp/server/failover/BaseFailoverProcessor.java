package com.jagornet.dhcp.server.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public abstract class BaseFailoverProcessor implements FailoverMessageProcessor
{
	private static Logger log = LoggerFactory.getLogger(BaseFailoverProcessor.class);

	protected final FailoverMessage requestMsg;
	protected FailoverMessage replyMsg;
	protected final String peerAddress;
	protected DhcpServerConfiguration serverConfig;
	protected FailoverStateManager fsm;
	
	protected BaseFailoverProcessor(FailoverMessage requestMsg) {
		this.requestMsg = requestMsg;
		this.peerAddress = DhcpServerPolicies.globalPolicy(Property.FAILOVER_PEER_SERVER);
		this.serverConfig = DhcpServerConfiguration.getInstance();
		this.fsm = serverConfig.getFailoverStateManager();
	}
	
    public FailoverMessage processMessage()
    {
    	try {
        	if (!preProcess()) {
        		log.warn("Message dropped by preProcess");
        		return null;
        	}
            
    		if (log.isDebugEnabled()) {
    			log.debug("Processing: " + requestMsg.toStringWithOptions());
    		}
    		else if (log.isInfoEnabled()) {
    	        log.info("Processing: " + requestMsg.toString());
    		}
	        
	        // build a reply message using the local and remote sockets from the request
	        replyMsg = new FailoverMessage(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
	        // copy the transaction ID into the reply
	        replyMsg.setTransactionId(requestMsg.getTransactionId());
	
	        /* TODO: Consider using peer ServerIDs in the message ala DHCPv6?
	         * 
	        // MUST put Server Identifier DUID in ADVERTISE or REPLY message
	        replyMsg.putDhcpOption(dhcpServerIdOption);
	        
	        // copy Client Identifier DUID if given in client request message
	        replyMsg.putDhcpOption(requestMsg.getDhcpClientIdOption());
	         */
	
	        if (!process()) {
	        	log.warn("Message dropped by processor");
	        	return null;
	        }
	        
	        if (log.isDebugEnabled()) {
	        	log.debug("Returning: " + replyMsg.toStringWithOptions());
	        }
	        else if (log.isInfoEnabled()) {
	        	log.info("Returning: " + replyMsg.toString());
	        }
	        
    	}
    	finally {
	        if (!postProcess()) {
	    		log.warn("Message dropped by postProcess");
	        	replyMsg = null;
	        }
    	}
        
        return replyMsg;
    }
	
    public boolean preProcess()
    {
    	//TODO: this check is redundant with FailoverChannelDecoder.acceptInboundMessage
    	String remoteIp = requestMsg.getRemoteAddress().getAddress().getHostAddress();
    	if (!remoteIp.equals(peerAddress)) {
        	log.error("Failover message received from unknown peer."  + 
        			  " Remote address=" + remoteIp +
        			  " Failover peer server=" + peerAddress);
        	return false;
        }
		if (fsm == null) {
			log.error("FailvoerStateManager not configured!");
			return false;
		}
    	return true;
    }
    
    /**
     * Process.
     * 
     * @return true if a reply should be sent
     */
    public abstract boolean process();
    
    /**
     * Post process.
     * 
     * @return true if a reply should be sent
     */
    public boolean postProcess()
    {
    	return true;
    }

}
