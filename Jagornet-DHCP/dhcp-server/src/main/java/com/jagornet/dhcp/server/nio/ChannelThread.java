package com.jagornet.dhcp.server.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.server.request.DhcpV4MessageHandler;

public class ChannelThread implements Runnable {

	private static Logger log = LoggerFactory.getLogger(ChannelThread.class);
	
	static int BUF_SZ = 1500;	// max MTU

    class Packet {
        ByteBuffer buf;
        SocketAddress sa;

        public Packet() {
            buf = ByteBuffer.allocate(BUF_SZ);
        }
        
        public Packet(ByteBuffer buf) {
        	this.buf = buf;
        }
    }

	private DatagramChannel channel;
	private Selector selector;
	private SelectionKey selectionKey;
	private LinkedBlockingQueue<Packet> inboundQueue;
	private LinkedBlockingQueue<Packet> outboundQueue;
	private Thread processThread;
	private ExecutorService threadPool;
	
	public ChannelThread(DatagramChannel channel) {
		this.channel = channel;
		inboundQueue = new LinkedBlockingQueue<Packet>(1000);	//TODO
		outboundQueue = new LinkedBlockingQueue<Packet>(1000);	//TODO
		threadPool = Executors.newCachedThreadPool();
		processThread = new Thread(new ProcessThread(), 
				Thread.currentThread().getName() + "-process");
		processThread.start();
	}
	
	public DatagramChannel getChannel() {
		return channel;
	}
	
	public void shutdown() {
		try { channel.close(); } catch (IOException ex) { }
		processThread.interrupt();
		threadPool.shutdown();
	}
	
	@Override
	public void run() {
		
		try {
			selector = Selector.open();
			selectionKey = channel.register(selector, SelectionKey.OP_READ);
            while (true) {
                try {
                	if (log.isDebugEnabled())
                		log.debug("Waiting on selector...");
                    int ready = selector.select();
                    if (ready <= 0) {
                    	if (log.isDebugEnabled())
                    		log.debug("Selector returned zero keys");
                    	continue;
                    }
                    
                    Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        try {
                            SelectionKey key = (SelectionKey) selectedKeys.next();
                            selectedKeys.remove();

                            if (!key.isValid()) {
                            	continue;
                            }

                            if (key.isReadable()) {
                            	if (log.isDebugEnabled())
                            		log.debug("Key is readable");
                                read(key);
                                //key.interestOps(SelectionKey.OP_WRITE);
                            } 
                            else if (key.isWritable()) {
                            	if (log.isDebugEnabled())
                            		log.debug("Key is writable");
                                write(key);
                                //key.interestOps(SelectionKey.OP_READ);
                            }
                        } 
                        catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("Continuing...");
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }				
		}
		catch (Exception ex) {
			ex.printStackTrace();
            System.err.println("Abort!");
		}
	}
	
	private void read(SelectionKey key) throws IOException {
		Packet packet = new Packet();
		packet.sa = channel.receive(packet.buf);
		if (packet.sa != null) {
			packet.buf.flip();
			if (!inboundQueue.offer(packet)) {
				log.error("Inbound queue is full");
			}
		}
	}
	
	private void write(SelectionKey key) throws IOException {
		Packet packet = outboundQueue.poll();
		if (packet != null) {
			channel.send(packet.buf, packet.sa);
		}
		else {
			selectionKey.interestOps(SelectionKey.OP_READ);
		}
	}
	
	class ProcessThread implements Runnable {
		
		public ProcessThread() {
			
		}

		@Override
		public void run() {
			while (true) {
				try {
					Packet packet = inboundQueue.take();
					if (packet != null) {
						threadPool.submit(new PacketProcessor(packet));
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}

	}
	
	class PacketProcessor implements Runnable {
		
		private Packet packet;
		public PacketProcessor(Packet packet) {
			this.packet = packet;
		}
		
		@Override
		public void run() {
			try {
				DhcpV4Message reqMessage = decode(packet);
				if (reqMessage != null) {
					DhcpV4Message respMessage = handle(reqMessage);
					if (respMessage != null) {
						Packet packet = encode(respMessage);
						if (!outboundQueue.offer(packet)) {
							log.error("Outbound queue is full");
						}
						else {
							selectionKey.interestOps(SelectionKey.OP_WRITE);
						}
					}
					else {
						log.warn("Response message is null");
					}
				}
				else {
					log.error("Failed to decode packet");
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		private DhcpV4Message decode(Packet packet) throws IOException {
	        DhcpV4Message dhcpMessage =  
	            	DhcpV4Message.decode(packet.buf, 
	            			(InetSocketAddress)channel.socket().getLocalSocketAddress(),
	            			(InetSocketAddress)packet.sa);
	        return dhcpMessage;
		}
		
		private DhcpV4Message handle(DhcpV4Message reqMessage) {
	        DhcpV4Message replyMessage = 
	        	DhcpV4MessageHandler.handleMessage(reqMessage.getLocalAddress().getAddress(), 
	        										reqMessage);
	        return replyMessage;
		}
		
		private Packet encode(DhcpV4Message respMessage) throws IOException {
			ByteBuffer buf = respMessage.encode();
			Packet packet = new Packet(buf);
			packet.sa = respMessage.getRemoteAddress();
			return packet;
		}
	}
}
