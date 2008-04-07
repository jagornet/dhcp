package com.agr.dhcpv6.option;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.apache.mina.common.IoBuffer;

import com.agr.dhcpv6.util.DhcpConstants;

public class TestDhcpStatusCodeOption extends TestCase
{
    public void testEncode() throws Exception
    {
        DhcpStatusCodeOption dsco = new DhcpStatusCodeOption();
        dsco.getStatusCodeOption().setStatusCode(DhcpConstants.STATUS_CODE_SUCCESS);
        dsco.getStatusCodeOption().setMessage("All is well");
        ByteBuffer bb = dsco.encode().buf();
        assertNotNull(bb);
        assertEquals(17, bb.capacity());
        assertEquals(17, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_STATUS_CODE, bb.getShort());
        assertEquals((short)13, bb.getShort());   // length
        assertEquals(DhcpConstants.STATUS_CODE_SUCCESS, bb.getShort());
        byte[] buf = new byte[11];
        bb.get(buf);
        assertEquals("All is well", new String(buf));
    }

    public void testDecode() throws Exception
    {
        // just 15 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(15);
        bb.putShort((short)13);  // length
        bb.putShort((short)DhcpConstants.STATUS_CODE_SUCCESS);
        bb.put("All is well".getBytes());
        bb.flip();
        DhcpStatusCodeOption dsco = new DhcpStatusCodeOption();
        dsco.decode(IoBuffer.wrap(bb));
        assertNotNull(dsco.getStatusCodeOption());
        assertEquals(13, dsco.getLength());
        assertEquals(DhcpConstants.STATUS_CODE_SUCCESS, 
                     dsco.getStatusCodeOption().getStatusCode());
        assertEquals("All is well", dsco.getStatusCodeOption().getMessage());
    }
}
