package com.jagornet.dhcp.server.db;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpFileChannels {
	
	private static Logger log = LoggerFactory.getLogger(IpFileChannels.class);

	private Map<InetAddress, AsynchronousFileChannel> ipFileChannels;
	
	protected IpFileChannels(int numberOfChannels) {
		ipFileChannels = Collections.synchronizedMap(
				new LinkedHashMap<InetAddress, AsynchronousFileChannel>(16, 0.75f, true) {
					private static final long serialVersionUID = 1L;
					@Override
				     protected boolean removeEldestEntry(
				    		 Map.Entry<InetAddress, AsynchronousFileChannel> eldest) {
						
				        if (size() > numberOfChannels) {
				        	try {
								eldest.getValue().close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        	return true;
				        }
				        return false;
				     }
					
				});
	}
	
	public void putIpFileChannel(InetAddress ip, AsynchronousFileChannel channel) {
		ipFileChannels.put(ip, channel);
	}
	
	public AsynchronousFileChannel getIpFileChannel(InetAddress ip) {
		return ipFileChannels.get(ip);
	}
	
	public void removeIpFileChannel(InetAddress ip) {
		AsynchronousFileChannel fc = ipFileChannels.remove(ip);
		if (fc != null) {
			try {
				fc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
