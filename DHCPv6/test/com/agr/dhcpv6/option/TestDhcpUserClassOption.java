package com.agr.dhcpv6.option;

import java.nio.ByteBuffer;
import java.util.List;

import junit.framework.TestCase;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import com.agr.dhcpv6.dto.UserClassOptionDTO;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.UserClassOption;
import com.agr.dhcpv6.util.DhcpConstants;

public class TestDhcpUserClassOption extends TestCase
{
    public void testEncode() throws Exception
    {
        DhcpUserClassOption duco = new DhcpUserClassOption();
        duco.addUserClass("UserClass 1");   // len = 2 + 11
        duco.addUserClass("UserClass 2");   // len = 2 + 11
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
        assertNotNull(duco.getUserClassOption());
        List<OpaqueData> userClasses = duco.getUserClassOption().getUserClasses();
        assertNotNull(userClasses);
        assertEquals(2, userClasses.size());
        assertEquals("UserClass 1", userClasses.get(0).getAsciiValue());
        assertEquals("UserClass 2", userClasses.get(1).getAsciiValue());
    }
    
    public void testToDTO() throws Exception
    {
        UserClassOption uco = new UserClassOption();
        OpaqueData uc1 = new OpaqueData();
        uc1.setAsciiValue("UserClass 1");
        uco.getUserClasses().add(uc1);
        OpaqueData uc2 = new OpaqueData();
        uc2.setAsciiValue("UserClass 2");
        uco.getUserClasses().add(uc2);
        MapperIF mapper = new DozerBeanMapper();
        UserClassOptionDTO dto = (UserClassOptionDTO)
                                    mapper.map(uco, UserClassOptionDTO.class);
        assertNotNull(dto);
        assertNotNull(dto.getUserClasses());
        assertEquals(2, dto.getUserClasses().size());
        assertEquals("UserClass 1", dto.getUserClasses().get(0).getAsciiValue());
        assertEquals("UserClass 2", dto.getUserClasses().get(1).getAsciiValue());
    }
}
