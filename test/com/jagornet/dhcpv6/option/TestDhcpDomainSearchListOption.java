package com.jagornet.dhcpv6.option;

import java.nio.ByteBuffer;
import java.util.List;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.util.DhcpConstants;

public class TestDhcpDomainSearchListOption extends TestCase
{
	String domain1;
	String domain2;
	
    @Override
	protected void setUp() throws Exception
	{
		super.setUp();
		domain1 = "jagornet.com.";		// 08jagornet03com00 8+3+3 =  14
		domain2 = "ipv6universe.com.";	// 0Cipv6universe03com00 12+3+3 = 18
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testEncode() throws Exception
    {
        DhcpDomainSearchListOption dslo = new DhcpDomainSearchListOption();
        dslo.addDomainName(domain1);
        dslo.addDomainName(domain2);
        ByteBuffer bb = dslo.encode();
        assertNotNull(bb);
        assertEquals(36, bb.capacity());    // +4 (code=2bytes, len=2bytes)
        assertEquals(36, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_DOMAIN_SEARCH_LIST, bb.getShort());
        assertEquals((short)32, bb.getShort());   // length
        assertEquals((byte)8, bb.get());
        byte[] buf = new byte[8];
        bb.get(buf);
        assertEquals("jagornet", new String(buf));
        assertEquals((byte)3, bb.get());
        buf = new byte[3];
        bb.get(buf);
        assertEquals("com", new String(buf));
        assertEquals((byte)0, bb.get());
        
        assertEquals((byte)12, bb.get());
        buf = new byte[12];
        bb.get(buf);
        assertEquals("ipv6universe", new String(buf));
        assertEquals((byte)3, bb.get());
        buf = new byte[3];
        bb.get(buf);
        assertEquals("com", new String(buf));
        assertEquals((byte)0, bb.get());
    }

    public void testDecode() throws Exception
    {
        // just 34 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(34);
        bb.putShort((short)32);     // length of option
        bb.put((byte)8);
        bb.put("jagornet".getBytes());
        bb.put((byte)3);
        bb.put("com".getBytes());
        bb.put((byte)0);
        bb.put((byte)12);
        bb.put("ipv6universe".getBytes());
        bb.put((byte)3);
        bb.put("com".getBytes());
        bb.put((byte)0);
        bb.flip();
        DhcpDomainSearchListOption dslo = new DhcpDomainSearchListOption();
        dslo.decode(bb);
        assertNotNull(dslo.getDomainNameListOption());
        assertEquals(2, dslo.getDomainNameListOption().getDomainNameList().size());
        List<String> domainNames = dslo.getDomainNameListOption().getDomainNameList();
        assertEquals(domain1, domainNames.get(0)); 
        assertEquals(domain2, domainNames.get(1)); 
    }
}
