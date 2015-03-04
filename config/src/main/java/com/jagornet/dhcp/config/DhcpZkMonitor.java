package com.jagornet.dhcp.config;

import com.jagornet.dhcp.util.Subnet;
import com.jagornet.dhcp.xml.Link;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by menabe on 28.02.15.
 */
public class DhcpZkMonitor {
    private static final Logger log = LoggerFactory.getLogger(DhcpZkMonitor.class);

    private final CuratorFramework curatorFramework;
    private final PathChildrenCache pathChildrenCache;
    private final String base;
    //private final SwappableHandlerByPath swappableHandlerByPath;

    private final Map<String, DhcpLink> dhcpLinks = new HashMap<>();

    public Map<String, DhcpLink> getDhcpLinks() {
        return dhcpLinks;
    }

    public DhcpZkMonitor(String zkPath, String base) throws Exception {
        this.base = base;
        curatorFramework = CuratorFrameworkFactory.newClient("192.168.176.115:2181", new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        final String configPath = base+"/config";
        if (curatorFramework.checkExists().forPath(configPath) == null) {
            curatorFramework.create().creatingParentsIfNeeded().forPath(configPath);
        }

        pathChildrenCache = new PathChildrenCache(curatorFramework, configPath, true);
        pathChildrenCache.start();
        log.info("Monitor:"+configPath);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            private  String[] network(String path) {
                String[] subnetParts = new File(path).getName().split("-");
                if (subnetParts.length != 2) {
                    log.error("can not parse the subnetParts", path);
                    return null;
                }
                if (subnetParts[0].startsWith("/")) {
                    subnetParts[0] = subnetParts[0].substring(1);
                }
                //log.debug("p"+path+":"+subnetParts[0]+":"+subnetParts[1]);
                return subnetParts;
            }

            private void updateData(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if (event.getData().getData() == null) {
                    return;
                }
                final String[] subnetParts = network(event.getData().getPath());
                if (subnetParts == null) {
                    return;
                }
                Subnet subnet = new Subnet(subnetParts[0], subnetParts[1]);
                String data = new String(client.getData().forPath(event.getData().getPath()));
                //log.error("read:"+data);
                Link link = Link.Factory.parse(data);
                dhcpLinks.put(subnet.network(), new DhcpLink(subnet, link));
                log.debug("read:" + subnet.network() + ":" + subnet.hashCode());
            }

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                //log.debug("childEvent"+event);
                switch (event.getType()) {
                    case CHILD_ADDED: {
                        log.debug("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        updateData(client, event);
                        break;
                    }

                    case CHILD_UPDATED: {
                        log.debug("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        updateData(client, event);
                        break;
                    }

                    case CHILD_REMOVED: {
                        log.debug("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        final String[] subnetParts = network(event.getData().getPath());
                        if (subnetParts == null) {
                            return;
                        }
                        Subnet subnet = new Subnet(subnetParts[0], subnetParts[1]);
                        dhcpLinks.remove(subnet.network());
                        break;
                    }
                }
            }
        });

//         zkClient.setZkSerializer(new Copy2Zk.StringSerializer());
//        final String configPath = base + "/config";
//        swappableHandlerByPath = new SwappableHandlerByPath(this);
//        zkClient.subscribeChildChanges(configPath, new IZkChildListener() {
//            @Override
//            public void handleChildChange(String s, List<String> list) throws Exception {
//                log.debug(">>HCC>>" + s + ":" + list);
//                final SwappableHandlerByPath.Transaction tr = swappableHandlerByPath.begin();
//                if (list != null) {
//                    for (String d : list) {
//                        final String cPath = configPath + "/" + d;
//                        tr.add(cPath);
//                    }
//                }
//                tr.commit();
//            }
//        });
//        zkClient.subscribeDataChanges(configPath, new IZkDataListener() {
//            @Override
//            public void handleDataChange(String s, Object o) throws Exception {
//                log.debug("handleDataChange>>>"+s);
//            }
//
//            @Override
//            public void handleDataDeleted(String s) throws Exception {
//
//            }
//        });
    }




}
