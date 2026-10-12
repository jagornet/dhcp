/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.testutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.SortedMap;

import jakarta.xml.bind.JAXBException;

import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;

/**
 * Lightweight test fixture context for safely configuring, testing,
 * and resetting the {@link DhcpServerConfiguration} and {@link DhcpServerPolicies}
 * singletons between test runs.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpConfigTestContext implements AutoCloseable {

	public DhcpConfigTestContext() {
		reset();
	}

	/**
	 * Reset both DhcpServerPolicies and DhcpServerConfiguration to clean default states.
	 */
	public static void reset() {
		try {
			Field defaultPropsField = DhcpServerPolicies.class.getDeclaredField("DEFAULT_PROPERTIES");
			defaultPropsField.setAccessible(true);
			Properties defaultProps = (Properties) defaultPropsField.get(null);

			Field serverPropsField = DhcpServerPolicies.class.getDeclaredField("SERVER_PROPERTIES");
			serverPropsField.setAccessible(true);
			serverPropsField.set(null, new Properties(defaultProps));

			Field instanceField = DhcpServerConfiguration.class.getDeclaredField("INSTANCE");
			instanceField.setAccessible(true);
			instanceField.set(null, null);

			syncStaticReferences();
		} catch (Exception e) {
			throw new RuntimeException("Failed to reset DhcpServerConfiguration or DhcpServerPolicies via reflection", e);
		}
	}

	private static void syncStaticReferences() {
		DhcpServerConfiguration currentConfig = DhcpServerConfiguration.getInstance();
		setStaticField("com.jagornet.dhcp.server.request.binding.BaseBindingManager", "serverConfig", currentConfig);
		setStaticField("com.jagornet.dhcp.server.request.BaseDhcpV4Processor", "dhcpServerConfig", currentConfig);
		setStaticField("com.jagornet.dhcp.server.request.BaseDhcpV6Processor", "dhcpServerConfig", currentConfig);
	}

	private static void setStaticField(String className, String fieldName, Object val) {
		try {
			Class<?> clazz = Class.forName(className);
			Field f = clazz.getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(null, val);
		} catch (ClassNotFoundException | NoSuchFieldException e) {
			// class or field not present, ignore
		} catch (Exception e) {
			throw new RuntimeException("Failed to set static field " + fieldName + " on " + className, e);
		}
	}

	private static void setInstanceField(Object target, String fieldName, Object val) {
		try {
			Field f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, val);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set instance field " + fieldName + " on " + target.getClass().getName(), e);
		}
	}

	/**
	 * Initialize the server configuration from an XML string snippet.
	 */
	public DhcpServerConfig initXml(String xml) throws JAXBException, DhcpServerConfigException, IOException {
		reset();
		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		DhcpServerConfig config = DhcpServerConfiguration.loadXmlConfig(bais);
		DhcpServerConfiguration.getInstance().init(config);
		setInstanceField(DhcpServerConfiguration.getInstance(), "jaxbServerConfig", config);
		syncStaticReferences();
		return config;
	}

	/**
	 * Initialize the server configuration from a YAML string snippet.
	 */
	public DhcpServerConfig initYaml(String yaml) throws DhcpServerConfigException, JAXBException, IOException {
		reset();
		ByteArrayInputStream bais = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
		DhcpServerConfig config = DhcpServerConfiguration.loadYamlConfig(bais);
		DhcpServerConfiguration.getInstance().init(config);
		setInstanceField(DhcpServerConfiguration.getInstance(), "jaxbServerConfig", config);
		syncStaticReferences();
		return config;
	}

	/**
	 * Initialize the server configuration from a JSON string snippet.
	 */
	public DhcpServerConfig initJson(String json) throws DhcpServerConfigException, JAXBException, IOException {
		reset();
		ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		DhcpServerConfig config = DhcpServerConfiguration.loadJsonConfig(bais);
		DhcpServerConfiguration.getInstance().init(config);
		setInstanceField(DhcpServerConfiguration.getInstance(), "jaxbServerConfig", config);
		syncStaticReferences();
		return config;
	}

	/**
	 * Initialize the server configuration directly from a JAXB {@link DhcpServerConfig} object.
	 */
	public void initConfig(DhcpServerConfig config) throws DhcpServerConfigException, JAXBException, IOException {
		reset();
		DhcpServerConfiguration.getInstance().init(config);
		setInstanceField(DhcpServerConfiguration.getInstance(), "jaxbServerConfig", config);
		syncStaticReferences();
	}

	/**
	 * Get the current singleton {@link DhcpServerConfiguration}.
	 */
	public DhcpServerConfiguration getConfiguration() {
		return DhcpServerConfiguration.getInstance();
	}

	/**
	 * Get the current link map.
	 */
	public SortedMap<Subnet, DhcpLink> getLinkMap() {
		return DhcpServerConfiguration.getInstance().getLinkMap();
	}

	@Override
	public void close() {
		reset();
	}
}
