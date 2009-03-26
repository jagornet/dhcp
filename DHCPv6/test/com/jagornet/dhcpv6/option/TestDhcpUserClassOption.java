package com.jagornet.dhcpv6.option;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import com.jagornet.dhcpv6.dto.option.UserClassOptionDTO;
import com.jagornet.dhcpv6.dto.option.base.OpaqueDataDTO;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.UserClassOption;

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
    
    public void testToDTO() throws Exception
    {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        
        UserClassOption uco = UserClassOption.Factory.newInstance();
        OpaqueData uc1 = uco.addNewOpaqueData();
        uc1.setAsciiValue("UserClass 1");
        OpaqueData uc2 = uco.addNewOpaqueData();
        uc2.setAsciiValue("UserClass 2");
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        UserClassOptionDTO dto = (UserClassOptionDTO)
                                    mapper.map(uco, UserClassOptionDTO.class);
        assertNotNull(dto);
        assertNotNull(dto.getOpaqueDataList());
        assertEquals(2, dto.getOpaqueDataList().size());
        // we only need to cast it here because we can't use Java 5
        // generics with GWT classes yet
        assertEquals("UserClass 1", 
                ((OpaqueDataDTO)dto.getOpaqueDataList().get(0)).getAsciiValue());
        assertEquals("UserClass 2", 
                ((OpaqueDataDTO)dto.getOpaqueDataList().get(1)).getAsciiValue());
    }
    
    public void testFromDTO() throws Exception
    {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        
        UserClassOptionDTO dto = new UserClassOptionDTO();
        List<OpaqueDataDTO> userClasses = new ArrayList<OpaqueDataDTO>();
        OpaqueDataDTO oddto1 = new OpaqueDataDTO();
        oddto1.setAsciiValue("UserClass 1");
        userClasses.add(oddto1);
        OpaqueDataDTO oddto2 = new OpaqueDataDTO();
        oddto2.setAsciiValue("UserClass 2");
        userClasses.add(oddto2);
        dto.setOpaqueDataList(userClasses);
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        UserClassOption uco = (UserClassOption)
                                mapper.map(dto, UserClassOption.class);
        assertNotNull(uco);
        assertNotNull(uco.getOpaqueDataList());
        assertEquals(2, uco.getOpaqueDataList().size());
        assertEquals("UserClass 1",
                    ((OpaqueData)uco.getOpaqueDataList().get(0)).getAsciiValue());
        assertEquals("UserClass 2",
                    ((OpaqueData)uco.getOpaqueDataList().get(1)).getAsciiValue());
    }
}
