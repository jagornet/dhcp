/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: BaseDomainNameListOption
 * Description: The abstract base class for domain name list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDomainNameListOption extends BaseDhcpOption
{
	protected List<String> domainNameList;

	public BaseDomainNameListOption() {
		this(null);
	}
	
	public BaseDomainNameListOption(List<String> domainNames) {
		super();
		this.domainNameList = domainNames;
	}
	
	public List<String> getDomainNameList() {
		return domainNameList;
	}

	public void setDomainNameList(List<String> domainNames) {
		this.domainNameList = domainNames;
	}

	public void addDomainName(String domainName) {
		if (domainName != null) {
			if (domainNameList == null) {
				domainNameList = new ArrayList<String>();
			}
			domainNameList.add(domainName);
		}
	}

	@Override
	public ByteBuffer encode() throws IOException
    {
    	ByteBuffer buf = super.encodeCodeAndLength();
        if (domainNameList != null) {
            for (String domain : domainNameList) {
                BaseDomainNameOption.encodeDomainName(buf, domain);
            }
        }
        return (ByteBuffer) buf.flip();
    }

	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf); 
    	if ((len > 0) && (len <= buf.remaining())) {
            int eof = buf.position() + len;
            while (buf.position() < eof) {
                String domain = BaseDomainNameOption.decodeDomainName(buf, eof);
                addDomainName(domain);
            }
        }
    }

	@Override
    public int getLength()
    {
        int len = 0;
        if (domainNameList != null) {
            for (String domain : domainNameList) {
                len += BaseDomainNameOption.getDomainNameLength(domain);
            }
        }
        return len;
    }

	@Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": domainNameList=");
        if (domainNameList != null) {
        	for (String domain : domainNameList) {
				sb.append(domain);
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
