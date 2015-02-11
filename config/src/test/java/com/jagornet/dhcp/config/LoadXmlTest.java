package com.jagornet.dhcp.config;

import com.jagornet.dhcp.exception.DhcpServerConfigException;
import org.apache.xmlbeans.XmlException;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LoadXmlTest {

    private static String getResourcePath(String fname) throws URISyntaxException {
        final URL resourceUrl = LoadXmlTest.class.getResource(fname);
        final Path path = Paths.get(resourceUrl.toURI());
        return path.toString();
    }

    @Test
    public void test_loadFile() throws XmlException, DhcpServerConfigException, IOException, URISyntaxException, KeeperException, InterruptedException {
        final String base = "/" + UUID.randomUUID().toString();
        try {
            DhcpServerConfiguration dsc = new DhcpServerConfiguration();
            dsc.init(getResourcePath("/test-config.xml"));
            Copy2Zk.start(dsc, "192.168.176.115", base, true);
            Copy2Zk.start(dsc, "192.168.176.115", base, false);
        } finally {
            Copy2Zk.stop("192.168.176.115", base);
        }
    }

}