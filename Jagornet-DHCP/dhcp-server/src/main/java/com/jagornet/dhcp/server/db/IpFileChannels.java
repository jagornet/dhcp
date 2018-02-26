package com.jagornet.dhcp.server.db;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class IpFileChannels {

	private static IpFileChannels instance = null;
	
	private static int MAX_FILE_CHANNELS = 1000;
	private static Map<InetAddress, AsynchronousFileChannel> ipFileChannels;
	
	protected IpFileChannels() {
		
	}
	
	public static IpFileChannels getInstance() {
		if (instance == null) {
			ipFileChannels = Collections.synchronizedMap(
					new LinkedHashMap<InetAddress, AsynchronousFileChannel>(16, 0.75f, true) {
						private static final long serialVersionUID = 1L;
						@Override
					     protected boolean removeEldestEntry(
					    		 Map.Entry<InetAddress, AsynchronousFileChannel> eldest) {
							
					        if (size() > MAX_FILE_CHANNELS) {
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
			instance = new IpFileChannels();
		}
		return instance;
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
