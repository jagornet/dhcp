package com.jagornet.dhcp.config;

import com.jagornet.dhcp.model.DhcpLease;
import com.jagornet.dhcp.model.option.DhcpUidOption;
import com.jagornet.dhcp.model.option.base.BaseOpaqueData;
import com.jagornet.dhcp.model.option.ddns.DhcpDDnsClientFqdn;
import com.jagornet.dhcp.model.option.ddns.DhcpDDnsFwdName;
import com.jagornet.dhcp.model.option.ddns.DhcpDDnsRevName;
import com.jagornet.dhcp.model.option.ddns.DhcpDDnsTxt;
import com.jagornet.dhcp.model.option.v4.DhcpV4HostnameOption;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

class LeaseV4Parser {

    private static final Logger log = LoggerFactory.getLogger(LeaseV4Parser.class);

    private static final DateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss", Locale.ENGLISH);

        //        # The format of this file is documented in the dhcpd.leases(5) manual page.
//                # This lease file was written by isc-dhcp-4.2.4
//
//        lease 192.168.36.44 {
//            starts 2 2014/12/16 10:07:27;
//            ends 2 2014/12/16 10:09:27;
//            tstp 2 2014/12/16 10:09:27;
//            cltt 2 2014/12/16 10:07:27;
//            binding state free;
//            hardware ethernet 64:76:ba:a5:bc:ca;
//            uid "\001dv\272\245\274\312";
//            client-hostname "Gerds-Air-2";
//        }
        public DhcpLease next(BufferedReader is) throws IOException, ParseException {
            final DhcpLease ret = new DhcpLease();
            String line;
            boolean inBlock = false;
            while ((line = is.readLine()) != null) {
                String[] st = line.trim().split("[;\\s]+");
                if (st.length < 1) {
                    continue;
                }
                if (st.length >= 2 && st[0].equals("lease")) {
                    inBlock = true;
                    ret.setIpAddress(InetAddress.getByName(st[1]));
                } else if (inBlock && st[0].equals("}")) {
                    return ret;
                } else if (inBlock && st.length >= 2) {
                    switch (st[0]) {
                        case "ends": {
                            ret.setValidEndTime(df.parse(st[2] + ' ' + st[3]));
                            break;
                        }
                        case "tstp": {
                            ret.setPreferredEndTime(df.parse(st[2] + ' ' + st[3]));
                            break;
                        }
                        case "cltt": {
                            break;
                        }
                        case "binding": {
                            break;
                        }
                        case "rewind": {
                            break;
                        }
                        case "next": {
                            break;
                        }
                        case "starts": {
                            ret.setStartTime(df.parse(st[2] + ' ' + st[3]));
                            break;
                        }
                        case "hardware": {
                            if (st[1].equals("ethernet")) {
                                String[] macStr = st[2].split(":");
                                byte[] mac = new byte[macStr.length];
                                for (int i = macStr.length - 1; i >= 0; --i) {
                                    mac[i] = DatatypeConverter.parseHexBinary(macStr[i])[0];
                                }
                                ret.setDuid(mac);
                            }
                            break;
                        }
                        case "set": {
                            switch (st[1]) {
                                case "ddns-rev-name": {
                                    DhcpDDnsRevName dDnsRevName = new DhcpDDnsRevName();
                                    dDnsRevName.setString(st[3].substring(1,st[3].length()-1));
                                    ret.addIaDhcpOption(dDnsRevName);
                                    break;
                                }
                                case "ddns-txt": {
                                    DhcpDDnsTxt dDnsTxt = new DhcpDDnsTxt();
                                    dDnsTxt.setString(st[3].substring(1,st[3].length()-1));
                                    ret.addIaDhcpOption(dDnsTxt);
                                    break;
                                }
                                case "ddns-fwd-name": {
                                    DhcpDDnsFwdName dDnsFwdName = new DhcpDDnsFwdName();
                                    dDnsFwdName.setString(st[3].substring(1,st[3].length()-1));
                                    ret.addIaDhcpOption(dDnsFwdName);
                                    break;
                                }
                                case "ddns-client-fqdn": {
                                    DhcpDDnsClientFqdn dhcpDDnsClientFqdn = new DhcpDDnsClientFqdn();
                                    dhcpDDnsClientFqdn.setString(st[3].substring(1,st[3].length()-1));
                                    ret.addIaDhcpOption(dhcpDDnsClientFqdn);
                                    break;
                                }
                                default: {
                                    log.error("unknown set:" + st[1]);
                                }
                            }
                            break;
                        }
                        case "uid": {
                            DhcpUidOption uidOption = new DhcpUidOption();
                            uidOption.setOpaqueData(new BaseOpaqueData(StringEscapeUtils.unescapeJava(st[1]).getBytes()));
                            ret.addIaDhcpOption(uidOption);
                            break;
                        }
                        case "client-hostname": {
                            DhcpV4HostnameOption op = new DhcpV4HostnameOption();
                            op.setName(st[1].substring(1,st[1].length()-1));
                            op.setV4(true);
                            op.setCode(47);
                            //op.setString("XXX");
                            ret.addIaDhcpOption(op);
                            break;
                        }
                        default: {
                            log.error("unknown:" + st[1]);
                        }
                    }

                }
            }
            return null;
        }
    }
