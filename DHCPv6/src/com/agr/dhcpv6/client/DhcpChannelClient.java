package com.agr.dhcpv6.client;

import java.io.IOException;
import java.net.SocketException;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.option.DhcpClientIdOption;
import com.agr.dhcpv6.option.DhcpElapsedTimeOption;
import com.agr.dhcpv6.option.DhcpUserClassOption;
import com.agr.dhcpv6.server.DhcpChannel;
import com.agr.dhcpv6.server.config.xml.ClientIdOption;
import com.agr.dhcpv6.server.config.xml.ElapsedTimeOption;
import com.agr.dhcpv6.server.config.xml.UserClassOption;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DhcpChannelClient implements Runnable
{
	private DhcpChannel dhcpChannel;

    public DhcpChannelClient()
		    throws IOException, SocketException
    {
		dhcpChannel = new DhcpChannel(DhcpConstants.CLIENT_PORT);
    }

	public void run()
	{
        DhcpMessage msg = new DhcpMessage(DhcpConstants.LOCALHOST,
                                          DhcpConstants.SERVER_PORT);
        for (int i=0; i<100; i++) {
            sendInfoRequest(i, msg);            
        }
	}

	private void sendInfoRequest(int id, DhcpMessage msg)
	{
        msg.setMessageType(DhcpConstants.INFO_REQUEST);
        msg.setTransactionId(id);
        byte[] clientIdBytes = { (byte)0xde,
                                 (byte)0xbd,
                                 (byte)0xeb,
                                 (byte)0xde,
                                 (byte)0xbd,
                                 (byte)0xeb };
        ClientIdOption clientIdOption = new ClientIdOption();
        clientIdOption.setHexValue(clientIdBytes);
        
        msg.setOption(new DhcpClientIdOption(clientIdOption));
        
        ElapsedTimeOption elapsedTimeOption = new ElapsedTimeOption();
        elapsedTimeOption.setValue((short)100);
        msg.setOption(new DhcpElapsedTimeOption(elapsedTimeOption));

        UserClassOption userClassOption = new UserClassOption();
        // wrap it with the DhcpOption subclass to get at
        // utility method for adding userclass string
        DhcpUserClassOption dhcpUserClassOption = 
            new DhcpUserClassOption(userClassOption);
        dhcpUserClassOption.addUserClass("FilterUserClass");
        msg.setOption(dhcpUserClassOption);
        
        try {
            // send the Info-Request
            dhcpChannel.send(msg);
            // go get the reply from the server
            dhcpChannel.receive(new DhcpMessage());
        }
        catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
	}

    public static void main(String[] args)
    {
		try {
			DhcpChannelClient client = new DhcpChannelClient();
            // single threaded - need new multithreaded architecture anyway
			client.run();
		}
		catch (Exception ex) {
			System.err.println(ex);
            ex.printStackTrace();
		}
    }
}
