package com.jagornet.dhcp.config;

import com.jagornet.dhcp.util.Subnet;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by menabe on 10.02.15.
 */
public class Copy2Zk implements Watcher {

    private static final Logger log = LoggerFactory.getLogger(DhcpServerConfiguration.class);

    private static class StringSerializer implements ZkSerializer {

        @Override
        public byte[] serialize(Object o) throws ZkMarshallingError {
            return ((String)o).getBytes();
        }

        @Override
        public Object deserialize(byte[] bytes) throws ZkMarshallingError {
            return new String(bytes);
        }
    }

    private Copy2Zk(DhcpServerConfiguration dsc, String zkServers, String zkRoot, boolean reset) throws IOException, KeeperException, InterruptedException {
        final ZkClient zk = new ZkClient(zkServers, 3600, Integer.MAX_VALUE);
        zk.setZkSerializer(new StringSerializer());
        if (!zk.exists(zkRoot)) {
            zk.createPersistent(zkRoot);
        }
        zkRoot += "/config";
        if (reset) {
            log.debug("delete-config=>"+zkRoot);
            zk.deleteRecursive(zkRoot);
        }
        if (!zk.exists(zkRoot)) {
            zk.createPersistent(zkRoot);
        }
        final Set<String> to_delete = new HashSet<String>();
        for(String name : zk.getChildren(zkRoot)) {
          to_delete.add(zkRoot + "/" + name);
        }
        for (Entry<Subnet, DhcpLink> entry : dsc.getLinkMap().entrySet())  {
            final String subnetFile = zkRoot + "/" + entry.getKey().getSubnetAddress().getHostAddress() + "-" + entry.getKey().getPrefixLength();
            log.debug("subnetFile =>"+subnetFile);
            to_delete.remove(subnetFile);
            String xml = "";
            if (zk.exists(subnetFile)) {
                xml = zk.readData(subnetFile);
            } else {
                zk.createPersistent(subnetFile);
            }
            final String write_xml = entry.getValue().toXml();
            //log.debug("xml=>"+xml+" wri=>"+write_xml);
            if (!xml.equals(write_xml)) {
                log.debug("write-xml=>"+subnetFile);
                zk.writeData(subnetFile, write_xml);

            }
        }
        for (String network_dir : to_delete) {
            log.debug("delete=>"+network_dir);
            zk.delete(network_dir);
        }
        zk.close();
    }

    public static Copy2Zk start(DhcpServerConfiguration dsc, String zkServers, String zkUrl, boolean reset) throws IOException, KeeperException, InterruptedException {
        return new Copy2Zk(dsc, zkServers, zkUrl, reset);
    }
    public static void stop(String zkServers, String zkUrl) {
        (new ZkClient(zkServers)).deleteRecursive(zkUrl);
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
