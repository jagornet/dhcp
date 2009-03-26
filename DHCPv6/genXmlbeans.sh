#!/bin/sh

# Use this line to generate the source and the xmltypes.jar file
/Users/agrabil/apache/xmlbeans-2.4.0/bin/scomp -javasource 1.5 -out build/lib/xmltypes.jar -src src/ conf/dhcpv6server.xsd conf/xmlbeans.xsdconfig

# Use this line to generate just the xmltypes.jar file
#/Users/agrabil/apache/xmlbeans-2.4.0/bin/scomp -javasource 1.5 -out build/lib/xmltypes.jar conf/dhcpv6server.xsd conf/xmlbeans.xsdconfig
