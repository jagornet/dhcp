package com.jagornet.dhcpv6.option;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.util.DhcpConstants;

public class TestDhcpPreferenceOption extends TestCase
{
    public void testEncode() throws Exception
    {
        DhcpPreferenceOption dpo = new DhcpPreferenceOption();
        dpo.getUnsignedByteOption().setUnsignedByte((byte)2);
        ByteBuffer bb = dpo.encode();
        assertNotNull(bb);
        assertEquals(5, bb.capacity());
        assertEquals(5, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_PREFERENCE, bb.getShort());
        assertEquals((short)1, bb.getShort());   // length
        assertEquals((byte)2, bb.get());
    }

    public void testDecode() throws Exception
    {
        // just three bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.putShort((short)1);  // length
        bb.put((byte)2);
        bb.flip();
        DhcpPreferenceOption dpo = new DhcpPreferenceOption();
        dpo.decode(bb);
        assertNotNull(dpo.getUnsignedByteOption());
        assertEquals(1, dpo.getLength());
        assertEquals(2, dpo.getUnsignedByteOption().getUnsignedByte());
    }
}
