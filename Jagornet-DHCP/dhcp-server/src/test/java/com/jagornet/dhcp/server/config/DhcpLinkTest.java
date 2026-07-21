package com.jagornet.dhcp.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.server.config.xml.Link;


public class DhcpLinkTest {

    private DhcpLink dhcpLink;
    private Subnet mockSubnet;
    private Link mockLink;

    @Before
    public void setUp() {
        mockSubnet = Mockito.mock(Subnet.class);
        mockLink = Mockito.mock(Link.class);
        Mockito.when(mockLink.getV6MsgConfigOptions()).thenReturn(null);
        Mockito.when(mockLink.getV6IaNaConfigOptions()).thenReturn(null);
        Mockito.when(mockLink.getV6NaAddrConfigOptions()).thenReturn(null);
        Mockito.when(mockLink.getV6IaTaConfigOptions()).thenReturn(null);
        Mockito.when(mockLink.getV6TaAddrConfigOptions()).thenReturn(null);
        Mockito.when(mockLink.getV6IaPdConfigOptions()).thenReturn(null);
        Mockito.when(mockLink.getV6PrefixConfigOptions()).thenReturn(null);
        Mockito.when(mockLink.getV4ConfigOptions()).thenReturn(null);
        dhcpLink = new DhcpLink(mockSubnet, mockLink);
    }

    @Test
    public void testDhcpLinkInitialization() {
        assertNotNull(dhcpLink);
        assertEquals(DhcpLink.State.OK, dhcpLink.getState());
    }

    @Test
    public void testGetLinkAddress() {
        Mockito.when(mockLink.getAddress()).thenReturn("192.168.1.1");
        assertEquals("192.168.1.1", dhcpLink.getLinkAddress());
    }

    @Test
    public void testGetAndSetSubnet() {
        Subnet newSubnet = Mockito.mock(Subnet.class);
        dhcpLink.setSubnet(newSubnet);
        assertEquals(newSubnet, dhcpLink.getSubnet());
    }

    @Test
    public void testGetAndSetLink() {
        Link newLink = Mockito.mock(Link.class);
        dhcpLink.setLink(newLink);
        assertEquals(newLink, dhcpLink.getLink());
    }

    @Test
    public void testGetAndSetMsgConfigOptions() {
        DhcpV6ConfigOptions newOptions = Mockito.mock(DhcpV6ConfigOptions.class);
        dhcpLink.setMsgConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getMsgConfigOptions());
    }

    @Test
    public void testGetAndSetIaNaConfigOptions() {
        DhcpV6ConfigOptions newOptions = Mockito.mock(DhcpV6ConfigOptions.class);
        dhcpLink.setIaNaConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getIaNaConfigOptions());
    }

    @Test
    public void testGetAndSetIaTaConfigOptions() {
        DhcpV6ConfigOptions newOptions = Mockito.mock(DhcpV6ConfigOptions.class);
        dhcpLink.setIaTaConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getIaTaConfigOptions());
    }

    @Test
    public void testGetAndSetIaPdConfigOptions() {
        DhcpV6ConfigOptions newOptions = Mockito.mock(DhcpV6ConfigOptions.class);
        dhcpLink.setIaPdConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getIaPdConfigOptions());
    }

    @Test
    public void testGetAndSetNaAddrConfigOptions() {
        DhcpV6ConfigOptions newOptions = Mockito.mock(DhcpV6ConfigOptions.class);
        dhcpLink.setNaAddrConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getNaAddrConfigOptions());
    }

    @Test
    public void testGetAndSetTaAddrConfigOptions() {
        DhcpV6ConfigOptions newOptions = Mockito.mock(DhcpV6ConfigOptions.class);
        dhcpLink.setTaAddrConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getTaAddrConfigOptions());
    }

    @Test
    public void testGetAndSetPrefixConfigOptions() {
        DhcpV6ConfigOptions newOptions = Mockito.mock(DhcpV6ConfigOptions.class);
        dhcpLink.setPrefixConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getPrefixConfigOptions());
    }

    @Test
    public void testGetAndSetV4ConfigOptions() {
        DhcpV4ConfigOptions newOptions = Mockito.mock(DhcpV4ConfigOptions.class);
        dhcpLink.setV4ConfigOptions(newOptions);
        assertEquals(newOptions, dhcpLink.getV4ConfigOptions());
    }

    @Test
    public void testGetAndSetState() {
        dhcpLink.setState(DhcpLink.State.NOT_SYNCED);
        assertEquals(DhcpLink.State.NOT_SYNCED, dhcpLink.getState());
    }
}