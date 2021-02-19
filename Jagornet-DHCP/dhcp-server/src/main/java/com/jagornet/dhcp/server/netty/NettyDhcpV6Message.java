package com.jagornet.dhcp.server.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.message.DhcpV6TransactionId;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaPdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaTaOption;
import com.jagornet.dhcp.core.util.Util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * A subclass of DhcpV6Message that uses Netty's ByteBuf for
 * encoding outbound messages, in attempt to gain performance.
 * 
 * @author agrabil
 *
 */
public class NettyDhcpV6Message extends DhcpV6Message {

    private static Logger log = LoggerFactory.getLogger(NettyDhcpV6Message.class);

	protected ByteBuf messageByteBuf;
	protected ByteBuf optionsByteBuf;
	
	public NettyDhcpV6Message(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
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
        messageByteBuf.writeByte((byte)messageType);
        messageByteBuf.writeBytes(DhcpV6TransactionId.encode(transactionId));
        messageByteBuf.writeBytes(encodeOptions());
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
        optionsByteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(1020); // 1024 - 1(msgType) - 3(transId) = 1020 (options)
        if (dhcpOptions != null) {
            for (DhcpOption option : dhcpOptions.values()) {
            	optionsByteBuf.writeBytes(option.encode());
            }
        }
        if (iaNaOptions != null) {
        	for (DhcpV6IaNaOption iaNaOption : iaNaOptions) {
        		optionsByteBuf.writeBytes(iaNaOption.encode());
			}
        }
        if (iaTaOptions != null) {
        	for (DhcpV6IaTaOption iaTaOption : iaTaOptions) {
        		optionsByteBuf.writeBytes(iaTaOption.encode());
			}
        }
        if (iaPdOptions != null) {
        	for (DhcpV6IaPdOption iaPdOption : iaPdOptions) {
        		optionsByteBuf.writeBytes(iaPdOption.encode());
			}
        }
        return optionsByteBuf;
    }
}
