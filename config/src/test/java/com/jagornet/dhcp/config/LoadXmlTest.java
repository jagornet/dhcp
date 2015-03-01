package com.jagornet.dhcp.config;

import com.jagornet.dhcp.exception.DhcpServerConfigException;
import org.apache.xmlbeans.XmlException;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;


public class LoadXmlTest {

    private static final Logger log = LoggerFactory.getLogger(LoadXmlTest.class);


    private static String getResourcePath(String fname) throws URISyntaxException {
        final URL resourceUrl = LoadXmlTest.class.getResource(fname);
        final Path path = Paths.get(resourceUrl.toURI());
        return path.toString();
    }


    @Test
    public void test_loadFile() throws XmlException, DhcpServerConfigException, IOException, URISyntaxException, KeeperException, InterruptedException {
        String base = "/" + UUID.randomUUID().toString();
        try {
            final DhcpZkMonitor monitor = new DhcpZkMonitor("192.168.176.115", base);

            DhcpServerConfiguration dsc = new DhcpServerConfiguration();
            dsc.init(getResourcePath("/test-config.xml"));
            Copy2Zk.start(dsc, "192.168.176.115", base, true);
            Copy2Zk.start(dsc, "192.168.176.115", base, false);
            while (monitor.getDhcpLinks().size() != 3) {
                log.debug("waiting:"+monitor.getDhcpLinks().size());
                Thread.sleep(200);
            }
            assertEquals(3, monitor.getDhcpLinks().size());
            assertNotNull(monitor.getDhcpLinks().get("10.0.0.0/24"));
            assertEquals("255.255.255.0",
            monitor.getDhcpLinks().get("10.0.0.0/24").getV4ConfigOptions()
                    .getV4ConfigOptions().getV4SubnetMaskOption().getIpAddress().toString());

        } finally {
            Copy2Zk.stop("192.168.176.115", base);
        }
    }


}