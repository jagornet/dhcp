package com.jagornet.dhcp.config;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class LoadXmlTest {

    private static final Logger log = LoggerFactory.getLogger(LoadXmlTest.class);


    private static String getResourcePath(String fname) throws URISyntaxException {
        final URL resourceUrl = LoadXmlTest.class.getResource(fname);
        final Path path = Paths.get(resourceUrl.toURI());
        return path.toString();
    }


    @Test
    public void testLoadConfigFile() throws Exception {
        String base = "/" + UUID.randomUUID().toString();
        try {
            final DhcpZkMonitor monitor = new DhcpZkMonitor("192.168.176.115", base);

            DhcpServerConfiguration dsc = new DhcpServerConfiguration();
            dsc.init(getResourcePath("/test-config.xml"));
            Copy2Zk.start(dsc, "192.168.176.115", base, false);
            while (monitor.getDhcpLinks().size() != 3) {
                log.debug("waiting:" + monitor.getDhcpLinks().size());
                Thread.sleep(200);
            }
            assertEquals(3, monitor.getDhcpLinks().size());
            assertNotNull(monitor.getDhcpLinks().get("10.0.0.0/24"));
            assertEquals("255.255.255.0",
                    monitor.getDhcpLinks().get("10.0.0.0/24").getV4ConfigOptions()
                            .getV4ConfigOptions().getV4SubnetMaskOption().getIpAddress().toString());
            assertEquals("2001:db8:1::1", monitor.getDhcpLinks().get("2001:db8:1:0:0:0:0:0/64").getMsgConfigOptions()
                    .getV6ConfigOptions().getV6DnsServersOption().getIpAddressArray(0));

            dsc = new DhcpServerConfiguration();
            dsc.init(getResourcePath("/test-config-minus.xml"));
            Copy2Zk.start(dsc, "192.168.176.115", base, false);

            while (monitor.getDhcpLinks().size() != 2) {
                log.debug("waiting:" + monitor.getDhcpLinks().size());
                Thread.sleep(200);
            }
            assertEquals(2, monitor.getDhcpLinks().size());
            assertNull(monitor.getDhcpLinks().get("10.0.0.0/24"));
            assertEquals("2001:db8:1::2", monitor.getDhcpLinks().get("2001:db8:1:0:0:0:0:0/64").getMsgConfigOptions()
                    .getV6ConfigOptions().getV6DnsServersOption().getIpAddressArray(0));



        } finally {
            try {
                Copy2Zk.stop("192.168.176.115", base);
            } catch (Throwable t) {
                // ignore
            }
        }
    }


}