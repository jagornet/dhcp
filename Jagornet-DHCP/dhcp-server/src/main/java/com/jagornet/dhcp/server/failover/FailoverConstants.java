package com.jagornet.dhcp.server.failover;

public class FailoverConstants {
    
	public static final int FAILOVER_PORT = 647;
	
	public static final String ROLE_PRIMARY = "primary";
	public static final String ROLE_BACKUP = "backup";
	
	/** Failover Message Types - use short to support unsigned byte. */
	
	/*
	 * Message types defined by the "orginal" RFC draft:
	 * https://www.ietf.org/archive/id/draft-ietf-dhc-failover-01.txt
	 */
	public static final short FAILOVER_MESSAGE_TYPE_DHCPBNDADD = 1;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPBNDUPD = 2;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPBNDDEL = 3;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPBNDACK = 4;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPBNDNAK = 5;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPPOLL = 6;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPPRPL = 7;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPCTLREQ = 8;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPCTLRET = 9;
	public static final short FAILOVER_MESSAGE_TYPE_DHCPCTLACK = 10;

	/*
	 * { "bindingUpdates": 
	 * 		{
		  		"version": 1,
		  		"adds": [
		  			{ 
		  				"id": 100,
	  					"duid": [-34,-69,30,-34,-69,30],
	  					"iatype": 0,
	  					"iaid": 1,
	  					"state": 2,
	  					"iaAddresses": [
	    					{
						      "id": 1000,
						      "ipAddress": "192.168.0.100",
						      "startTime": "Aug 3, 2018 6:16:53 PM",
						      "preferredEndTime": "Aug 3, 2018 6:16:57 PM",
						      "validEndTime": "Aug 3, 2018 6:16:57 PM",
						      "state": 2,
						      "identityAssocId": 100
	    					}
	    				]
	    			},
	    			{
	    				"id": 101,
	    				...
	    			},
	    			{	"id": 102,
	    				...
	    			}
	    		],
	    		"updates": [
	    			{
	    				"id": 103,
	    				...
	    			},
	    			...
	    		],
	    		"deletes": [
	    			{
	    				"id": 104,
	    				...
	    			},
	    			...
	    		]
    		}
     * 	}
	 */
	// Extended message types
	public static final short FAILOVER_MESSAGE_TYPE_DHCP_BULK_BINDING_CHANGES = 11;
	public static final short FAILOVER_MESSAGE_TYPE_DHCP_BULK_BINDING_ACKS = 12;
	
    
    public static final String[] FAILOVER_MESSAGE_STRING = { "Unknown", 
    														"Binding Add",
													        "Binding Update",
													        "Binding Delete",
													        "Binding Ack",
													        "Binding Nack",
													        "Poll",
													        "Poll Reply",
													        "Control Request",
													        "Control Return",
													        "Control Ack",
													        "Bulk Binding Changes",
													        "Bulk Binding Acks"};
    
    public static final String getFailoverMessageString(short msg)
    {
        if ( (msg > 0) && (msg <= FAILOVER_MESSAGE_TYPE_DHCP_BULK_BINDING_ACKS)) {
            return FAILOVER_MESSAGE_STRING[msg];
        }
        return "Unknown";
    }
}
