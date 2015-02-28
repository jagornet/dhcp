package com.jagornet.dhcp.config;

import ch.qos.logback.core.util.FileUtil;
import com.jagornet.dhcp.util.Subnet;
import com.jagornet.dhcp.xml.DhcpServerConfigDocument;
import com.jagornet.dhcp.xml.Link;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

/**
 * Created by menabe on 28.02.15.
 */
public class DhcpZkMonitor {
    private static final Logger log = LoggerFactory.getLogger(DhcpZkMonitor.class);

    private final ZkClient zkClient;
    private final String base;
    private final SwappableHandlerByPath swappableHandlerByPath;

    private final Map<String, DhcpLink> dhcpLinks = new HashMap<>();

    public Map<String, DhcpLink> getDhcpLinks() {
        return dhcpLinks;
    }

    public DhcpZkMonitor(String zkPath, String base) {
        zkClient = new ZkClient("192.168.176.115:2181");
        this.base = base;
        zkClient.setZkSerializer(new Copy2Zk.StringSerializer());
        final String configPath = base + "/config";
        swappableHandlerByPath = new SwappableHandlerByPath(this);
        zkClient.subscribeChildChanges(configPath, new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                log.debug(">>HCC>>" + s + ":" + list);
                final SwappableHandlerByPath.Transaction tr = swappableHandlerByPath.begin();
                if (list != null) {
                    for (String d : list) {
                        final String cPath = configPath + "/" + d;
                        tr.add(cPath);
                    }
                }
                tr.commit();
            }
        });
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

    public static class ZkConfigPathHandler {
        private final String path;
        private final IZkDataListener dataListener;
        private final DhcpZkMonitor monitor;

        private static String[] network(String path) {
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

        public void read(String path, Object data) {
            if (data == null) {
                log.error("read with null data:"+path);
                return;
            }

            try {
                String[] subnetParts = network(path);
                if (subnetParts == null) {
                    return;
                }
                Subnet subnet = new Subnet(subnetParts[0], subnetParts[1]);
                Link link = Link.Factory.parse(data.toString());
                monitor.dhcpLinks.put(subnet.network(), new DhcpLink(subnet, link));
                log.debug("read:"+subnet.network()+":"+subnet.hashCode());
            } catch (Throwable t) {
                log.error("deserialize from zookeeper of "+path+" failed."+t);
            }

        }
        public ZkConfigPathHandler(DhcpZkMonitor monitor, String path) {
            this.path = path;
            this.monitor = monitor;


            dataListener = new IZkDataListener() {
                @Override
                public void handleDataChange(String s, Object o) throws Exception {
                    log.debug(">handleDataChange>"+path+ ">" + s);
                    read(s, o);
                }
                @Override
                public void handleDataDeleted(String s) throws Exception {
                    log.debug(">handleDataDeleted>"+path+ ">" + s);
                    String[] subnetParts = network(path);
                    if (subnetParts != null) {
                        monitor.getDhcpLinks().remove(subnetParts[0] + "/" + subnetParts[1]);
                    }
                }
            };
            monitor.zkClient.subscribeDataChanges(path, dataListener);
            while (true) {
                final Object data = monitor.zkClient.readData(path);
                if (data != null) {
                    read(path, data);
                    break;
                }
                try {
                    log.debug("wait for data");
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    log.error("Interrupted"+e);
                }
            }

            //log.debug("ZkConfigPathHandler:" + path);
        }
        public void shutdown() {
            monitor.zkClient.unsubscribeDataChanges(path, dataListener);
        }

    }

    public static class SwappableHandlerByPath {
        private final Map<String, ZkConfigPathHandler> handlers = new HashMap<>();
        private final DhcpZkMonitor monitor;
        public SwappableHandlerByPath(DhcpZkMonitor monitor) {
            this.monitor = monitor;
        }
        public static class Transaction {
            private final SwappableHandlerByPath swappableHandlerByPath;
            private final List<ZkConfigPathHandler> listners = new LinkedList<>();
            public Transaction(SwappableHandlerByPath shbp) {
                swappableHandlerByPath = shbp;
            }
            public void commit() {
                for (ZkConfigPathHandler zkh : swappableHandlerByPath.handlers.values()) {
                    zkh.shutdown();
                }
                for (ZkConfigPathHandler zkh : listners) {
                    swappableHandlerByPath.handlers.put(zkh.path, zkh);
                }
            }
            public void add(String cPath) {
                ZkConfigPathHandler cl = swappableHandlerByPath.handlers.get(cPath);
                if (cl == null) {
                    cl = new ZkConfigPathHandler(swappableHandlerByPath.monitor, cPath);
                } else {
                    swappableHandlerByPath.handlers.remove(cPath);
                }
                listners.add(cl);
            }
        }
        public Transaction begin() {
            return new Transaction(this);
        }

    }

}
