package com.jagornet.dhcpv6.client;

import java.io.IOException;
import java.net.SocketException;

import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.ElapsedTimeOption;
import com.jagornet.dhcpv6.xml.UserClassOption;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpElapsedTimeOption;
import com.jagornet.dhcpv6.option.DhcpUserClassOption;
import com.jagornet.dhcpv6.server.DhcpChannel;
import com.jagornet.dhcpv6.util.DhcpConstants;


public class DhcpChannelClient implements Runnable
{
	private DhcpChannel dhcpChannel;

    public DhcpChannelClient()
		    throws IOException, SocketException
    {
//		dhcpChannel = new DhcpChannel(DhcpConstants.CLIENT_PORT);
		dhcpChannel = new DhcpChannel(10000 + DhcpConstants.CLIENT_PORT);
    }

	public void run()
	{
        DhcpMessage msg = new DhcpMessage(DhcpConstants.LOCALHOST,
//                                          DhcpConstants.SERVER_PORT);
        								  10000 + DhcpConstants.SERVER_PORT);
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
        ClientIdOption clientIdOption = ClientIdOption.Factory.newInstance();
        clientIdOption.setHexValue(clientIdBytes);
        
        msg.setOption(new DhcpClientIdOption(clientIdOption));
        
        ElapsedTimeOption elapsedTimeOption = ElapsedTimeOption.Factory.newInstance();
        elapsedTimeOption.setIntValue((short)100);
        msg.setOption(new DhcpElapsedTimeOption(elapsedTimeOption));

        UserClassOption userClassOption = UserClassOption.Factory.newInstance();
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
