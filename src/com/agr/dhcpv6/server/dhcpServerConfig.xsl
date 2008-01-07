<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xdt="http://www.w3.org/2005/xpath-datatypes">

<xsl:template match="/">
	<html>
		<head>
			<title>DHCPv6 Server Configuration</title>
			<style type="text/css">
				th.category {background-color: blue}
				th.headers {background-color: lightblue}
			</style>
		</head>
		<body>
			<table align="left" border="2" cellspacing="2" cellpadding="2">
				<tbody>
					<tr>
						<th class="category" colspan="4">Global Server Policies</th>
					</tr>
					<tr>
						<th class="headers" colspan="2">Policy</th>
						<th class="headers" colspan="2">Value</th>
					</tr>
					<tr>
						<td colspan="2">ABC Policy</td>
						<td colspan="2"><xsl:value-of select="dhcpV6ServerConfig/abcPolicy"/></td>
					</tr>
					<tr>
						<th class="category" colspan="4">Global Server Options</th>
					</tr>
					<tr>
						<th class="headers" colspan="2">Option</th>
						<th class="headers" colspan="2">Value</th>
					</tr>
					<tr>
						<td colspan="2">Server Identifier</td>
						<td colspan="2"><xsl:value-of select="dhcpV6ServerConfig/serverIdOption/hexValue"/></td>
					</tr>
					<xsl:for-each select="dhcpV6ServerConfig/dnsServersOption/serverIpAddresses">
						<tr>
							<td colspan="2">DNS Servers</td>
							<td colspan="2"><xsl:value-of select="."/></td>
						</tr>
					</xsl:for-each>
					<xsl:for-each select="dhcpV6ServerConfig/domainListOption/domainNames">
						<tr>
							<td colspan="2">Domain List</td>
							<td colspan="2"><xsl:value-of select="."/></td>
						</tr>
					</xsl:for-each>
					<tr>
						<th class="category" colspan="4">Global Filter Groups</th>
					</tr>
					<xsl:for-each select="dhcpV6ServerConfig/filterGroups">
						<tr>
							<th class="headers" colspan="4"><xsl:value-of select="name"/></th>
						</tr>
						<xsl:for-each select="optionExpressions">
							<tr>
								<td>Option Expressions</td>
								<td><xsl:value-of select="code"/></td>
								<td>equals</td>
								<td><xsl:value-of select="data/asciiValue"/></td>
							</tr>
						</xsl:for-each>
						<xsl:for-each select="domainListOption/domainNames">
							<tr>
								<td colspan="2">Domain List</td>
								<td colspan="2"><xsl:value-of select="."/></td>
							</tr>
						</xsl:for-each>
					</xsl:for-each>
				</tbody>
			</table>
		</body>
	</html>
</xsl:template>

</xsl:stylesheet>
