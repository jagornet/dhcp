package com.jagornet.dhcp.config;

import com.jagornet.dhcp.util.Subnet;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by menabe on 10.02.15.
 */
public class Copy2Zk implements Watcher {

    private static final Logger log = LoggerFactory.getLogger(DhcpServerConfiguration.class);


    private Copy2Zk(DhcpServerConfiguration dsc, String zkServers, String zkRoot, boolean reset) throws Exception {
        final CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkServers, new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        if (curatorFramework.checkExists().forPath(zkRoot) == null) {
            curatorFramework.create().creatingParentsIfNeeded().forPath(zkRoot);
        }
        zkRoot += "/config";
        if (reset) {
            log.debug("delete-config=>" + zkRoot);
            if (curatorFramework.checkExists().forPath(zkRoot) != null) {
                curatorFramework.delete().deletingChildrenIfNeeded().forPath(zkRoot);
            }
        }
        if (curatorFramework.checkExists().forPath(zkRoot) == null) {
            curatorFramework.create().forPath(zkRoot);
        }
        final Set<String> to_delete = new HashSet<>();
        for(String name : curatorFramework.getChildren().forPath(zkRoot)) {
          to_delete.add(zkRoot + "/" + name);
        }
        for (Entry<Subnet, DhcpLink> entry : dsc.getLinkMap().entrySet())  {
            final String subnetFile = zkRoot + "/" + entry.getKey().getSubnetAddress().getHostAddress() + "-" + entry.getKey().getPrefixLength();
            log.debug("subnetFile =>"+subnetFile);
            to_delete.remove(subnetFile);
            String xml = "";
            final String write_xml = entry.getValue().toXml();
            if (curatorFramework.checkExists().forPath(subnetFile) != null) {
                xml = new String(curatorFramework.getData().forPath(subnetFile));
            } else {
                curatorFramework.create().forPath(subnetFile, write_xml.getBytes());
                xml = write_xml;
            }
            //log.debug("xml=>"+xml+" wri=>"+write_xml);
            if (!xml.equals(write_xml)) {
                log.debug("write-xml=>"+subnetFile);
                curatorFramework.setData().forPath(subnetFile, write_xml.getBytes());
            }
        }
        for (String network_dir : to_delete) {
            log.debug("delete=>"+network_dir);
            curatorFramework.delete().deletingChildrenIfNeeded().forPath(network_dir);
        }
        curatorFramework.close();
    }

    public static Copy2Zk start(DhcpServerConfiguration dsc, String zkServers, String zkUrl, boolean reset) throws Exception {
        return new Copy2Zk(dsc, zkServers, zkUrl, reset);
    }
    public static void stop(String zkServers, String zkUrl) throws Exception {
        final CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkServers, new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        curatorFramework.delete().deletingChildrenIfNeeded().forPath(zkUrl);
        curatorFramework.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        log.debug("WatchedEvent:"+watchedEvent);
    }

    public static void main(String[] argv)  {
        if (argv.length < 3) {
            log.error("usage: <cfg_filename> <zkServers> <zkDstUrl> [<reset-flag>]");
        } else {
            log.debug("Load file[" + argv[0] + " to " + argv[1] + "["+argv[2]+"] reset=" + (argv.length > 3));
            try {
                final DhcpServerConfiguration dsc = new DhcpServerConfiguration();
                dsc.init(argv[0]);
                Copy2Zk.start(dsc, argv[1], argv[2], argv.length > 3);
                System.exit(0);
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }
        System.exit(1);
    }
}
