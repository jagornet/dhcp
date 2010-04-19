package com.jagornet.dhcpv6.option;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.base.DhcpOption;

import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.xml.ConfigOptionsType;

/**
 * The Class DhcpConfigOptions.
 */
public class DhcpConfigOptions
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpConfigOptions.class);
	
	// regular expression for matching option getter methods
	/** The get option pattern. */
	protected static Pattern getOptionPattern = Pattern.compile("get.*Option");
	
	/** The IMPL. */
	protected static String IMPL = "Impl";
	
	/** The GE t_ code. */
	protected static String GET_CODE = "getCode";
	
	/** The GE t_ length. */
	protected static String GET_LENGTH = "getLength";
	
	/** The OPTIO n_ pk g_ prefix. */
	protected static String OPTION_PKG_PREFIX = "com.jagornet.dhcpv6.option.Dhcp";
	
	/** The XM l_ pkg. */
	protected static String XML_PKG = "com.jagornet.dhcpv6.xml.";
    
    /** The requested option codes. */
    protected List<Integer> requestedOptionCodes;

	/** The config options. */
	protected ConfigOptionsType configOptions;
	
	/** The option map. */
	protected Map<Integer, DhcpOption> optionMap = new TreeMap<Integer, DhcpOption>();
	
	/**
	 * Instantiates a new dhcp config options.
	 */
	public DhcpConfigOptions()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp config options.
	 * 
	 * @param configOptions the config options
	 */
	public DhcpConfigOptions(ConfigOptionsType configOptions)
	{
		this(configOptions, null);
	}
	
	/**
	 * Instantiates a new dhcp config options.
	 * 
	 * @param configOptions the config options
	 * @param requestedOptionCodes the requested option codes
	 */
	public DhcpConfigOptions(ConfigOptionsType configOptions, List<Integer> requestedOptionCodes)
	{
		if (configOptions != null) 
			this.configOptions = configOptions;
		else
			this.configOptions = ConfigOptionsType.Factory.newInstance();
		
		if (requestedOptionCodes != null) {
			setRequestedOptionCodes(requestedOptionCodes);
		}
		
		initDhcpOptionMap();
	}
    
    /**
     * Inits the dhcp option map.
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> initDhcpOptionMap()
    {
		Method[] methods = configOptions.getClass().getMethods();
		if (methods != null) {
			optionMap.clear();
			for (Method method : methods) {
				// for each option getter: e.g. getPreferenceOption
				if (isOptionGetter(method)) {
					DhcpOption dhcpOption = buildDhcpOption(method);
					if (dhcpOption != null)
						optionMap.put(dhcpOption.getCode(), dhcpOption);
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
	public ConfigOptionsType getConfigOptions() {
		return configOptions;
	}

	/**
	 * Sets the config options.
	 * 
	 * @param configOptions the new config options
	 */
	public void setConfigOptions(ConfigOptionsType configOptions) {
		this.configOptions = configOptions;
		// reset the option map
		initDhcpOptionMap();
	}
	
	/**
	 * Gets the requested option codes.
	 * 
	 * @return the requested option codes
	 */
	public List<Integer> getRequestedOptionCodes() {
		return requestedOptionCodes;
	}

	/**
	 * Sets the requested option codes.
	 * 
	 * @param requestedOptionCodes the new requested option codes
	 */
	public void setRequestedOptionCodes(List<Integer> requestedOptionCodes) {
		this.requestedOptionCodes = requestedOptionCodes;
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
    
    /**
     * Check if the client requested a particular option in the OptionRequestOption.
     * If no OptionRequestOption was supplied by the client, then assume that it
     * wants any option.
     * 
     * @param optionCode the option code to check if the client requested
     * 
     * @return true, if successful
     */
    protected boolean clientWantsOption(int optionCode)
    {
    	if (requestedOptionCodes != null) {
    		// if the client requested it, then send it
    		return requestedOptionCodes.contains(optionCode);
    	}
    	
    	// if there is no ORO, then the client did not request the option,
    	// so now check if configured to send only requested options
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		// don't send the option
    		return false;
    	}
    	
    	// if we're not sending requested options only,
    	// then we're sending whatever we have configured
    	return true;
    }
    
	/**
	 * Gets the length.
	 * 
	 * @return the length
	 */
	public int getLength()
	{
		int len = 0;
/*
		Method[] methods = configOptions.getClass().getMethods();
		for (Method method : methods) {
			// for each option getter: e.g. getPreferenceOption
			if (isOptionGetter(method)) {
				DhcpOption dhcpOption = buildDhcpOption(method);
				if (dhcpOption != null)
					len += 2 + 2 + dhcpOption.getLength();	// code + len + valuelen
			}
		}
*/
		Iterator<DhcpOption> options = optionMap.values().iterator();
		while (options.hasNext()) {
			len += 2 + 2 + options.next().getLength();	// code + len + valuelen
		}
		return len;
	}

    /**
     * Encode.
     * 
     * @return the byte buffer
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(getLength());
/*
		Method[] methods = configOptions.getClass().getMethods();
		for (Method method : methods) {
			// for each option getter: e.g. getPreferenceOption
			if (isOptionGetter(method)) {
				DhcpOption dhcpOption = buildDhcpOption(method);
				if (dhcpOption != null)
					buf.put(dhcpOption.encode());
			}
		}
*/
		Iterator<DhcpOption> options = optionMap.values().iterator();
		while (options.hasNext()) {
			buf.put(options.next().encode());
		}
        return (ByteBuffer) buf.flip();
    }

/*
	public void setDhcpOptions(DhcpOptionable dhcpOptionalble)
	{
		Method[] methods = configOptions.getClass().getMethods();
		for (Method method : methods) {
			// for each option getter: e.g. getPreferenceOption
			if (isOptionGetter(method)) {
				DhcpOption dhcpOption = buildDhcpOption(method);
				if (dhcpOption != null)
					dhcpOptionalble.setDhcpOption(dhcpOption);
			}
		}
	}
*/
    
    /**
 * Checks if is option getter.
 * 
 * @param method the method
 * 
 * @return true, if is option getter
 */
private boolean isOptionGetter(Method method)
    {
		Matcher matcher = getOptionPattern.matcher(method.getName());
		return matcher.matches();
    }

    /**
     * Builds the dhcp option.
     * 
     * @param method the method
     * 
     * @return the dhcp option
     */
    private DhcpOption buildDhcpOption(Method method)
    {
    	DhcpOption dhcpOpt = null;
    	try {
			// get the configured option by invoking getter
			Object obj = method.invoke(configOptions, (Object[])null);
			if (obj != null) {
				dhcpOpt = buildDhcpOption(obj);
			}
    	}
    	catch (Exception ex) {
    		log.error("Failed to build DhcpOption: " + ex);
    	}
		return dhcpOpt;
    }
    
    /**
     * Builds the dhcp option.
     * 
     * @param obj the obj
     * 
     * @return the dhcp option
     * 
     * @throws Exception the exception
     */
    private DhcpOption buildDhcpOption(Object obj) throws Exception
    {
    	DhcpOption dhcpOpt = null;
		Method getCodeMethod = obj.getClass().getMethod(GET_CODE, (Class<?>[])null);
		Integer code = (Integer)getCodeMethod.invoke(obj, (Object[])null);
		if (clientWantsOption(code)) {
			// optName = PreferenceOptionImpl
			String optName = obj.getClass().getSimpleName();
			if (optName.endsWith(IMPL))
				optName = optName.substring(0, optName.length()-4);
			// dhcpOptName = com.jagornet.dhcpv6.option.DhcpPreferenceOption
			String dhcpOptName = OPTION_PKG_PREFIX + optName;
			Class<?> dhcpOptClass = Class.forName(dhcpOptName);
			String configOptName = XML_PKG + optName;
			Constructor<?> c = dhcpOptClass.getConstructor(Class.forName(configOptName));
			// create a DhcpPreferenceOption via the constructor
			// DhcpPreferenceOption(PreferenceOption)
			dhcpOpt = (DhcpOption)c.newInstance(obj);
		}
		return dhcpOpt;
    }
}
