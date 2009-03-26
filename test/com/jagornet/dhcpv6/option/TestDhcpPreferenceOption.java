package com.jagornet.dhcpv6.option;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import com.jagornet.dhcpv6.dto.option.PreferenceOptionDTO;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.PreferenceOption;

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
    
    public void testToDTO() throws Exception
    {
        PreferenceOption po = PreferenceOption.Factory.newInstance();
        po.setUnsignedByte((byte)2);
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        PreferenceOptionDTO dto = (PreferenceOptionDTO)
                                    mapper.map(po, PreferenceOptionDTO.class);
        assertEquals(Short.valueOf(po.getUnsignedByte()), dto.getValue());
    }
}
