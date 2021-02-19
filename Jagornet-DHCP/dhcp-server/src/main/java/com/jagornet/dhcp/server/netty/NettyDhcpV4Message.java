package com.jagornet.dhcp.server.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * A subclass of DhcpV4Message that uses Netty's ByteBuf for
 * encoding outbound messages, in attempt to gain performance.
 * 
 * @author agrabil
 * 
 */
public class NettyDhcpV4Message extends DhcpV4Message {

    private static Logger log = LoggerFactory.getLogger(NettyDhcpV4Message.class);

	protected ByteBuf messageByteBuf;
	protected ByteBuf optionsByteBuf;
	
	public NettyDhcpV4Message(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
		super(localAddress, remoteAddress);
	}
	
	public void release() {
		if (messageByteBuf != null) {
			messageByteBuf.release();
		}
		if (optionsByteBuf != null) {
			optionsByteBuf.release();
		}
	}
	
	@Override
    public ByteBuffer encode() throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Encoding DhcpMessage for: " + 
            		Util.socketAddressAsString(remoteAddress));
        
        messageByteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);
        messageByteBuf.writeByte((byte)op);
        messageByteBuf.writeByte((byte)htype);
        messageByteBuf.writeByte((byte)hlen);
        messageByteBuf.writeByte((byte)hops);
        messageByteBuf.writeInt((int)transactionId);
        messageByteBuf.writeShort((short)secs);
        messageByteBuf.writeShort((short)flags);
        if (ciAddr != null) {
        	messageByteBuf.writeBytes(ciAddr.getAddress());
        }
        else {
        	messageByteBuf.writeBytes(DhcpConstants.ZEROADDR_V4.getAddress());
        }
        if (yiAddr != null) {
        	messageByteBuf.writeBytes(yiAddr.getAddress());
        }
        else {
        	messageByteBuf.writeBytes(DhcpConstants.ZEROADDR_V4.getAddress());
        }
        if (siAddr != null) {
        	messageByteBuf.writeBytes(siAddr.getAddress());
        }
        else {
        	messageByteBuf.writeBytes(DhcpConstants.ZEROADDR_V4.getAddress());
        }
        if (giAddr != null) {
        	messageByteBuf.writeBytes(giAddr.getAddress());
        }
        else {
        	messageByteBuf.writeBytes(DhcpConstants.ZEROADDR_V4.getAddress());
        }
        messageByteBuf.writeBytes(Arrays.copyOf(chAddr, 16));	// pad to 16 bytes for encoded packet
        
        StringBuffer sNameBuf = new StringBuffer();
        if (sName != null) {
        	sNameBuf.append(sName);
        }
        sNameBuf.setLength(64-sNameBuf.length());
        messageByteBuf.writeBytes(sNameBuf.toString().getBytes());
        
        StringBuffer fileBuf = new StringBuffer();
        if (file != null) {
        	fileBuf.append(file);
        }
        fileBuf.setLength(128-fileBuf.length());
        messageByteBuf.writeBytes(fileBuf.toString().getBytes());

        messageByteBuf.writeBytes(encodeOptionsAsByteBuf());
    	int msglen = messageByteBuf.writerIndex();
        if (log.isDebugEnabled())
            log.debug("DHCPv4 Message is " + msglen + " bytes");
        if (msglen < 300) {
        	int pad = 300 - msglen;
            if (log.isDebugEnabled())
                log.debug("Padding with " + pad + " bytes to 300 byte (Bootp) minimum");
        	messageByteBuf.writeBytes(new byte[pad]);
        }
        // don't need to flip Netty ByteBuf
        // buf.flip();
        
        if (log.isDebugEnabled())
            log.debug("DhcpMessage encoded.");
        
        return messageByteBuf.nioBuffer();
    }

    /**
     * Encode the options of this DhcpMessage to wire format for sending.
     * 
     * @return	a ByteBuffer containing the encoded options
     * @throws IOException
     */
    protected ByteBuf encodeOptionsAsByteBuf() throws IOException
    {
        optionsByteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(788);	// 788 - 236 = 1020 (options)
        if (dhcpOptions != null) {
        	// magic cookie as per rfc1497
        	optionsByteBuf.writeBytes(magicCookie);
        	for (DhcpOption option : dhcpOptions.values()) {
        		optionsByteBuf.writeBytes(option.encode());
            }
        	optionsByteBuf.writeByte((byte)DhcpConstants.V4OPTION_EOF);	// end option
        }
        return optionsByteBuf;
    }
}
