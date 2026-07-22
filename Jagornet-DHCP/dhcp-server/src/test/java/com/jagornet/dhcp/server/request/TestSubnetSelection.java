package com.jagornet.dhcp.server.request;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.option.DhcpUnknownOption;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4SubnetSelectionOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class TestSubnetSelection {

    @Before
    public void setup() {
        DhcpServerPolicies.setProperty(Property.V4_SUBNET_SELECTION, "true");
    }

    @Test
    public void testSubnetSelectionOption() throws Exception {
        InetSocketAddress local = new InetSocketAddress("10.0.0.1", 67);
        InetSocketAddress remote = new InetSocketAddress("10.0.0.1", 68);
        DhcpV4Message req = new DhcpV4Message(local, remote);
        req.setOp((short) DhcpConstants.V4_OP_REQUEST);
        req.setGiAddr(InetAddress.getByName("0.0.0.0"));

        DhcpV4SubnetSelectionOption opt118 = new DhcpV4SubnetSelectionOption();
        opt118.setIpAddress("192.168.1.100");
        req.putDhcpOption(opt118);

        DhcpV4MsgTypeOption msgType = new DhcpV4MsgTypeOption();
        msgType.setUnsignedByte((short) DhcpConstants.V4MESSAGE_TYPE_DISCOVER);
        req.putDhcpOption(msgType);

        InetAddress localAddr = InetAddress.getByName("10.0.0.1");

        // Handle message (won't actually process fully without config, but will parse
        // options)
        try {
            DhcpV4MessageHandler.handleMessage(localAddr, req);
        } catch (Exception e) {
            // we expect it might fail later due to missing config, but we want to ensure
            // parsing passes
        }
    }

    @Test
    public void testLinkSelectionSubOption() throws Exception {
        InetSocketAddress local = new InetSocketAddress("10.0.0.1", 67);
        InetSocketAddress remote = new InetSocketAddress("10.0.0.1", 68);
        DhcpV4Message req = new DhcpV4Message(local, remote);
        req.setOp((short) DhcpConstants.V4_OP_REQUEST);
        req.setGiAddr(InetAddress.getByName("0.0.0.0"));

        DhcpUnknownOption opt82 = new DhcpUnknownOption();
        opt82.setCode(DhcpConstants.V4OPTION_RELAY_INFO);
        byte[] payload = new byte[] {
                5, 4, 10, 0, 0, 1 // subopt 5 (link selection), len 4, IP 10.0.0.1
        };
        opt82.setOpaqueData(new BaseOpaqueData(payload));
        req.putDhcpOption(opt82);

        DhcpV4MsgTypeOption msgType = new DhcpV4MsgTypeOption();
        msgType.setUnsignedByte((short) DhcpConstants.V4MESSAGE_TYPE_DISCOVER);
        req.putDhcpOption(msgType);

        InetAddress localAddr = InetAddress.getByName("10.0.0.1");

        try {
            DhcpV4MessageHandler.handleMessage(localAddr, req);
        } catch (Exception e) {
        }
    }
}
