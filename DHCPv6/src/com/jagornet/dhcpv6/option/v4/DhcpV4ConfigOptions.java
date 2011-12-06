package com.jagornet.dhcpv6.option.v4;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.generic.GenericDomainNameListOption;
import com.jagornet.dhcpv6.option.generic.GenericDomainNameOption;
import com.jagornet.dhcpv6.option.generic.GenericIpAddressListOption;
import com.jagornet.dhcpv6.option.generic.GenericIpAddressOption;
import com.jagornet.dhcpv6.option.generic.GenericOpaqueDataListOption;
import com.jagornet.dhcpv6.option.generic.GenericOpaqueDataOption;
import com.jagornet.dhcpv6.option.generic.GenericStringOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedByteOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedIntOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedShortListOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedShortOption;
import com.jagornet.dhcpv6.xml.DomainNameListOptionType;
import com.jagornet.dhcpv6.xml.DomainNameOptionType;
import com.jagornet.dhcpv6.xml.GenericOptionsType;
import com.jagornet.dhcpv6.xml.IpAddressListOptionType;
import com.jagornet.dhcpv6.xml.IpAddressOptionType;
import com.jagornet.dhcpv6.xml.OpaqueDataListOptionType;
import com.jagornet.dhcpv6.xml.OpaqueDataOptionType;
import com.jagornet.dhcpv6.xml.OptionDefType;
import com.jagornet.dhcpv6.xml.StringOptionType;
import com.jagornet.dhcpv6.xml.UnsignedByteOptionType;
import com.jagornet.dhcpv6.xml.UnsignedIntOptionType;
import com.jagornet.dhcpv6.xml.UnsignedShortListOptionType;
import com.jagornet.dhcpv6.xml.UnsignedShortOptionType;
import com.jagornet.dhcpv6.xml.V4ConfigOptionsType;
import com.jagornet.dhcpv6.xml.V4DomainNameOption;
import com.jagornet.dhcpv6.xml.V4DomainServersOption;
import com.jagornet.dhcpv6.xml.V4RoutersOption;
import com.jagornet.dhcpv6.xml.V4SubnetMaskOption;
import com.jagornet.dhcpv6.xml.V4TimeOffsetOption;
import com.jagornet.dhcpv6.xml.V4TimeServersOption;

/**
 * The Class DhcpV4ConfigOptions.
 */
public class DhcpV4ConfigOptions
{
	/** The config options. */
	protected V4ConfigOptionsType configOptions;
	
	/** The option map. */
	protected Map<Integer, DhcpOption> optionMap = new TreeMap<Integer, DhcpOption>();
	
	
	/**
	 * Instantiates a new dhcp config options.
	 */
	public DhcpV4ConfigOptions()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp config options.
	 * 
	 * @param configOptions the config options
	 */
	public DhcpV4ConfigOptions(V4ConfigOptionsType configOptions)
	{
		if (configOptions != null) 
			this.configOptions = configOptions;
		else
			this.configOptions = V4ConfigOptionsType.Factory.newInstance();
		
		initDhcpV4OptionMap();
	}
    
    /**
     * Inits the dhcp option map.
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> initDhcpV4OptionMap()
    {
		optionMap.clear();
		
		if (configOptions.isSetV4DomainNameOption()) {
			V4DomainNameOption domainNameOption = configOptions.getV4DomainNameOption();
			if (domainNameOption != null) {
				optionMap.put((int)domainNameOption.getCode(),
						new DhcpV4DomainNameOption(domainNameOption));
			}
		}
		
		if (configOptions.isSetV4DomainServersOption()) {
			V4DomainServersOption domainServersOption = configOptions.getV4DomainServersOption();
			if (domainServersOption != null) {
				optionMap.put((int)domainServersOption.getCode(),
						new DhcpV4DomainServersOption(domainServersOption));
			}
		}
		
		if (configOptions.isSetV4RoutersOption()) {
			V4RoutersOption routersOption = configOptions.getV4RoutersOption();
			if (routersOption != null) {
				optionMap.put((int)routersOption.getCode(),
						new DhcpV4RoutersOption(routersOption));
			}
		}
		
		if (configOptions.isSetV4SubnetMaskOption()) {
			V4SubnetMaskOption subnetMaskOption = configOptions.getV4SubnetMaskOption();
			if (subnetMaskOption != null) {
				optionMap.put((int)subnetMaskOption.getCode(),
						new DhcpV4SubnetMaskOption(subnetMaskOption));
			}
		}
		
		if (configOptions.isSetV4TimeOffsetOption()) {
			V4TimeOffsetOption timeOffsetOption = configOptions.getV4TimeOffsetOption();
			if (timeOffsetOption != null) {
				optionMap.put((int)timeOffsetOption.getCode(),
						new DhcpV4TimeOffsetOption(timeOffsetOption));
			}
		}
		
		if (configOptions.isSetV4TimeServersOption()) {
			V4TimeServersOption timeServersOption = configOptions.getV4TimeServersOption();
			if (timeServersOption != null) {
				optionMap.put((int)timeServersOption.getCode(),
						new DhcpV4TimeServersOption(timeServersOption));
			}
		}
		
		if (configOptions.isSetOtherOptions()) {
			GenericOptionsType genericOptions = configOptions.getOtherOptions();
			if (genericOptions != null) {
				List<OptionDefType> optionDefs = genericOptions.getOptionDefList();
				if ((optionDefs != null) && !optionDefs.isEmpty()) {
					for (OptionDefType optionDefType : optionDefs) {
						
						int code = optionDefType.getCode();
						String name = optionDefType.getName();
							
						// the XML schema defines the optionDefType as a choice,
						// so we must determine which generic option is set
						
						if (optionDefType.isSetDomainNameListOption()) {
							DomainNameListOptionType domainNameListOption =
								optionDefType.getDomainNameListOption();
							if (domainNameListOption != null) {
								GenericDomainNameListOption dhcpOption = 
									new GenericDomainNameListOption(code, name, domainNameListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetDomainNameOption()) {
							DomainNameOptionType domainNameOption =
								optionDefType.getDomainNameOption();
							if (domainNameOption != null) {
								GenericDomainNameOption dhcpOption = 
									new GenericDomainNameOption(code, name, domainNameOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetIpAddressListOption()) {
							IpAddressListOptionType ipAddressListOption =
								optionDefType.getIpAddressListOption();
							if (ipAddressListOption != null) {
								GenericIpAddressListOption dhcpOption = 
									new GenericIpAddressListOption(code, name, ipAddressListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetIpAddressOption()) {
							IpAddressOptionType ipAddressOption =
								optionDefType.getIpAddressOption();
							if (ipAddressOption != null) {
								GenericIpAddressOption dhcpOption = 
									new GenericIpAddressOption(code, name, ipAddressOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetOpaqueDataListOption()) {
							OpaqueDataListOptionType opaqueDataListOption =
								optionDefType.getOpaqueDataListOption();
							if (opaqueDataListOption != null) {
								GenericOpaqueDataListOption dhcpOption = 
									new GenericOpaqueDataListOption(code, name, opaqueDataListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetOpaqueDataOption()) {
							OpaqueDataOptionType opaqueDataOption =
								optionDefType.getOpaqueDataOption();
							if (opaqueDataOption != null) {
								GenericOpaqueDataOption dhcpOption = 
									new GenericOpaqueDataOption(code, name, opaqueDataOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetStringOption()) {
							StringOptionType stringOption =
								optionDefType.getStringOption();
							if (stringOption != null) {
								GenericStringOption dhcpOption =
									new GenericStringOption(code, name, stringOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetUByteOption()) {
							UnsignedByteOptionType uByteOption =
								optionDefType.getUByteOption();
							if (uByteOption != null) {
								GenericUnsignedByteOption dhcpOption = 
									new GenericUnsignedByteOption(code, name, uByteOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetUIntOption()) {
							UnsignedIntOptionType uIntOption =
								optionDefType.getUIntOption();
							if (uIntOption != null) {
								GenericUnsignedIntOption dhcpOption = 
									new GenericUnsignedIntOption(code, name, uIntOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetUShortListOption()) {
							UnsignedShortListOptionType uShortListOption =
								optionDefType.getUShortListOption();
							if (uShortListOption != null) {
								GenericUnsignedShortListOption dhcpOption = 
									new GenericUnsignedShortListOption(code, name, uShortListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetUShortOption()) {
							UnsignedShortOptionType uShortOption =
								optionDefType.getUShortOption();
							if (uShortOption != null) {
								GenericUnsignedShortOption dhcpOption = 
									new GenericUnsignedShortOption(code, name, uShortOption);
								optionMap.put(code, dhcpOption);
							}
						}
											
					}
				}
			}
		}
		
		return optionMap;
    }

	/**
	 * Gets the config options.
	 * 
	 * @return the config options
	 */
	public V4ConfigOptionsType getV4ConfigOptions() {
		return configOptions;
	}

	/**
	 * Sets the config options.
	 * 
	 * @param configOptions the new config options
	 */
	public void setV4ConfigOptions(V4ConfigOptionsType configOptions) {
		this.configOptions = configOptions;
		// reset the option map
		initDhcpV4OptionMap();
	}
	
	/**
	 * Gets the dhcp option map.
	 * 
	 * @return the dhcp option map
	 */
	public Map<Integer, DhcpOption> getDhcpOptionMap()
	{
		return optionMap;
	}
}
