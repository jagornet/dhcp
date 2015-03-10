package com.jagornet.dhcp.config;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;


public class LoadLeasesTest {

    private static final Logger log = LoggerFactory.getLogger(LoadLeasesTest.class);


    private static String getResourcePath(String fname) throws URISyntaxException {
        final URL resourceUrl = LoadLeasesTest.class.getResource(fname);
        final Path path = Paths.get(resourceUrl.toURI());
        return path.toString();
    }


    @Test
    public void testLoadLeasesFile() throws Exception {
        String base = "/" + UUID.randomUUID().toString();
        try {

            IscLeases2Zk.readV4("dhcpd.leases", "192.168.176.115", base, false);



        } finally {
            try {
                Config2Zk.cleanup("192.168.176.115", base);
            } catch (Throwable t) {
                // ignore
            }
        }
    }


}