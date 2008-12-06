package com.agr.dhcpv6.option;

import java.nio.ByteBuffer;

import junit.framework.TestCase;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import org.apache.mina.core.buffer.IoBuffer;

import com.agr.dhcpv6.dto.PreferenceOptionDTO;
import com.agr.dhcpv6.server.config.xml.PreferenceOption;
import com.agr.dhcpv6.util.DhcpConstants;

public class TestDhcpPreferenceOption extends TestCase
{
    public void testEncode() throws Exception
    {
        DhcpPreferenceOption dpo = new DhcpPreferenceOption();
        dpo.getPreferenceOption().setValue((byte)2);
        ByteBuffer bb = dpo.encode().buf();
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
        dpo.decode(IoBuffer.wrap(bb));
        assertNotNull(dpo.getPreferenceOption());
        assertEquals(1, dpo.getLength());
        assertEquals(2, dpo.getPreferenceOption().getValue());
    }
    
    public void testToDTO() throws Exception
    {
        PreferenceOption po = new PreferenceOption();
        po.setValue((byte)2);
        MapperIF mapper = new DozerBeanMapper();
        PreferenceOptionDTO dto = (PreferenceOptionDTO)
                                    mapper.map(po, PreferenceOptionDTO.class);
        assertEquals(Short.valueOf(po.getValue()), dto.getValue());
    }
}
