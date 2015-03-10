package com.jagornet.dhcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagornet.dhcp.model.DhcpLease;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.Base64;

/**
 * Created by menabe on 10.02.15.
 */
public class IscLeases2Zk {

    private static final Logger log = LoggerFactory.getLogger(DhcpServerConfiguration.class);


    public static void writeZk(CuratorFramework curatorFramework, String file, String data) throws Exception {
        String readData;
        if (curatorFramework.checkExists().forPath(file) != null) {
            readData = new String(curatorFramework.getData().forPath(file));
        } else {
            curatorFramework.create().forPath(file, data.getBytes());
            readData = data;
        }
        //log.debug("xml=>"+xml+" wri=>"+write_xml);
        if (!readData.equals(data)) {
            log.debug("write=>" + file);
            curatorFramework.setData().forPath(file, data.getBytes());
        }
    }

    public static void readV4(String filename, String zkServers, String zkRoot, boolean reset) throws Exception {
        final CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkServers, new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        if (curatorFramework.checkExists().forPath(zkRoot) == null) {
            curatorFramework.create().creatingParentsIfNeeded().forPath(zkRoot);
        }
        zkRoot += "/leases";
        if (reset) {
            log.debug("delete-config=>" + zkRoot);
            if (curatorFramework.checkExists().forPath(zkRoot) != null) {
                curatorFramework.delete().deletingChildrenIfNeeded().forPath(zkRoot);
            }
        }
        if (curatorFramework.checkExists().forPath(zkRoot) == null) {
            curatorFramework.create().forPath(zkRoot);
        }
        final String byIp = zkRoot + "/byIp";
        if (curatorFramework.checkExists().forPath(byIp) == null) {
            curatorFramework.create().forPath(byIp);
        }
        final String byDuid = zkRoot + "/byDuid";
        if (curatorFramework.checkExists().forPath(byDuid) == null) {
            curatorFramework.create().forPath(byDuid);
        }

        final LeaseV4Parser lv4p = new LeaseV4Parser();
        final ObjectMapper objectMapper = new ObjectMapper();
        int cnt = 0;
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        for (DhcpLease dl = lv4p.next(br); dl != null; dl = lv4p.next(br)) {
            String ipFile = byIp + "/" + dl.getIpAddress().getHostAddress();
            String duidFname = Base64.getUrlEncoder().encodeToString(dl.getDuid());
            String duidFile = byDuid + "/" + duidFname;
            writeZk(curatorFramework, ipFile, duidFname);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            objectMapper.writeValue(baos, dl);
            writeZk(curatorFramework, duidFile, baos.toString());
            ++cnt;
        }
        log.info("Read-Leases:" + cnt);
        curatorFramework.close();
    }


    public static void main(String[] argv) {
        if (argv.length < 2) {
            log.error("usage: <v4-lease>");
        } else {
            log.debug("Load file[" + argv[0] + "]");
            try {
                readV4(argv[0], null, null, false);
                System.exit(0);
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }
        System.exit(1);
    }
}
