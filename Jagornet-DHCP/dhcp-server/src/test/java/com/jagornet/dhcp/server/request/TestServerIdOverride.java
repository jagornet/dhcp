package com.jagornet.dhcp.server.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.option.DhcpUnknownOption;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class TestServerIdOverride {

    private class TestProcessor extends BaseDhcpV4Processor {
        public TestProcessor(DhcpV4Message requestMsg, InetAddress clientLinkAddress) {
            super(requestMsg, clientLinkAddress);
        }

        @Override
        public boolean process() {
            return true;
        }

        public InetAddress testGetServerIdOverride(DhcpV4Message requestMsg) {
            return getServerIdOverride(requestMsg);
        }
    }

    @Before
    public void setup() {
        DhcpServerPolicies.setProperty(Property.V4_SERVER_ID_OVERRIDE, "false");
    }

    @Test
    public void testServerIdOverrideDisabledByDefault() throws Exception {
        InetSocketAddress local = new InetSocketAddress("10.0.0.1", 67);
        InetSocketAddress remote = new InetSocketAddress("10.0.0.1", 68);
        DhcpV4Message req = new DhcpV4Message(local, remote);
        req.setOp((short) DhcpConstants.V4_OP_REQUEST);
        req.setGiAddr(InetAddress.getByName("10.0.0.1"));

        DhcpUnknownOption opt82 = new DhcpUnknownOption();
        opt82.setCode(DhcpConstants.V4OPTION_RELAY_INFO);
        byte[] payload = new byte[] {
                DhcpConstants.V4OPTION_RELAY_INFO_SERVER_ID_OVERRIDE, 4, (byte) 192, (byte) 168, 100, 1
        };
        opt82.setOpaqueData(new BaseOpaqueData(payload));
        req.putDhcpOption(opt82);

        TestProcessor processor = new TestProcessor(req, InetAddress.getByName("10.0.0.1"));
        assertNull(processor.testGetServerIdOverride(req));
    }

    @Test
    public void testServerIdOverrideEnabled() throws Exception {
        DhcpServerPolicies.setProperty(Property.V4_SERVER_ID_OVERRIDE, "true");

        InetSocketAddress local = new InetSocketAddress("10.0.0.1", 67);
        InetSocketAddress remote = new InetSocketAddress("10.0.0.1", 68);
        DhcpV4Message req = new DhcpV4Message(local, remote);
        req.setOp((short) DhcpConstants.V4_OP_REQUEST);
        req.setGiAddr(InetAddress.getByName("10.0.0.1"));

        DhcpUnknownOption opt82 = new DhcpUnknownOption();
        opt82.setCode(DhcpConstants.V4OPTION_RELAY_INFO);
        byte[] payload = new byte[] {
                DhcpConstants.V4OPTION_RELAY_INFO_SERVER_ID_OVERRIDE, 4, (byte) 192, (byte) 168, 100, 1
        };
        opt82.setOpaqueData(new BaseOpaqueData(payload));
        req.putDhcpOption(opt82);

        TestProcessor processor = new TestProcessor(req, InetAddress.getByName("10.0.0.1"));
        InetAddress overrideAddr = processor.testGetServerIdOverride(req);
        assertNotNull(overrideAddr);
        assertEquals("192.168.100.1", overrideAddr.getHostAddress());
    }

    @Test
    public void testSelectingRequestWithServerIdOverride() throws Exception {
        DhcpServerPolicies.setProperty(Property.V4_SERVER_ID_OVERRIDE, "true");

        InetSocketAddress local = new InetSocketAddress("10.0.0.1", 67);
        InetSocketAddress remote = new InetSocketAddress("10.0.0.1", 68);
        DhcpV4Message req = new DhcpV4Message(local, remote);
        req.setOp((short) DhcpConstants.V4_OP_REQUEST);
        req.setGiAddr(InetAddress.getByName("10.0.0.1"));
        req.setChAddr(new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 });

        DhcpV4MsgTypeOption msgType = new DhcpV4MsgTypeOption();
        msgType.setUnsignedByte((short) DhcpConstants.V4MESSAGE_TYPE_REQUEST);
        req.putDhcpOption(msgType);

        DhcpV4RequestedIpAddressOption reqIpOpt = new DhcpV4RequestedIpAddressOption();
        reqIpOpt.setIpAddress("192.168.100.50");
        req.putDhcpOption(reqIpOpt);

        DhcpV4ServerIdOption serverIdOpt = new DhcpV4ServerIdOption();
        serverIdOpt.setIpAddress("192.168.100.1");
        req.putDhcpOption(serverIdOpt);

        DhcpUnknownOption opt82 = new DhcpUnknownOption();
        opt82.setCode(DhcpConstants.V4OPTION_RELAY_INFO);
        byte[] payload = new byte[] {
                DhcpConstants.V4OPTION_RELAY_INFO_SERVER_ID_OVERRIDE, 4, (byte) 192, (byte) 168, 100, 1
        };
        opt82.setOpaqueData(new BaseOpaqueData(payload));
        req.putDhcpOption(opt82);

        DhcpV4RequestProcessor processor = new DhcpV4RequestProcessor(req, InetAddress.getByName("10.0.0.1"));
        try {
            processor.preProcess();
        } catch (Exception e) {
        }
    }
}
