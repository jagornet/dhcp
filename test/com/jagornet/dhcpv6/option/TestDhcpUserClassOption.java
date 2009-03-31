package com.jagornet.dhcpv6.option;

import java.nio.ByteBuffer;
import java.util.List;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.OpaqueData;

public class TestDhcpUserClassOption extends TestCase
{
    public void testEncode() throws Exception
    {
        DhcpUserClassOption duco = new DhcpUserClassOption();
        duco.addOpaqueData("UserClass 1");   // len = 2 + 11
        duco.addOpaqueData("UserClass 2");   // len = 2 + 11
        ByteBuffer bb = duco.encode();
        assertNotNull(bb);
        assertEquals(30, bb.capacity());    // +4 (code=2bytes, len=2bytes)
        assertEquals(30, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_USER_CLASS, bb.getShort());
        assertEquals((short)26, bb.getShort());   // length
        assertEquals((short)11, bb.getShort());
    }

    public void testDecode() throws Exception
    {
        // just 28 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(28);
        bb.putShort((short)26);     // length of option
        bb.putShort((short)11);     // length of "UserClass 1"
        bb.put("UserClass 1".getBytes());
        bb.putShort((short)11);
        bb.put("UserClass 2".getBytes());
        bb.flip();
        DhcpUserClassOption duco = new DhcpUserClassOption();
        duco.decode(bb);
        assertNotNull(duco.getOpaqueDataListOptionType());
        List<OpaqueData> userClasses = duco.getOpaqueDataListOptionType().getOpaqueDataList();
        assertNotNull(userClasses);
        assertEquals(2, userClasses.size());
        assertEquals("UserClass 1", userClasses.get(0).getAsciiValue());
        assertEquals("UserClass 2", userClasses.get(1).getAsciiValue());
    }
}
