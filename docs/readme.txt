Jagor DHCPv6
The Jagor DHCPv6 server is an implementation of a stateless Dynamic Host 
Configuration Protocol for IPv6 (DHCPv6) server as defined by RFC 3736.  
The server is designed for use in networks that utilize IPv6 Stateless 
Address Autoconfiguration (SLAAC) as defined by RFC 2462.

System Requirements
The implementation of the server is completely in Java and requires Java 5 
runtime environment to be installed and available on the machine that will 
host the Jagor DHCPv6 server.  Any host with a Java 5 compliant JVM can be
used as the server machine.  Tested platforms include openSuse 10 Linux,  
Solaris 10, and Windows XP, each running Sun's supported Java 5 runtime for
the respective platforms.

DHCPv6 Client Modes of Operation
Please refer to the following article from "The Cable Guy" which summarizes
the various modes of operation for IPv6 clients.
http://technet.microsoft.com/en-us/magazine/cc162485.aspx

Stateless DHCPv6 Operational Models
Hosts which use stateless address autoconfiguration to obtain an IPv6 address 
can use DHCPv6 to obtain other configuration parameters required for 
networking, such as a list of available DNS servers.  The client sends an 
Information-Request message to the All_DHCP_Relay_Agents_and_Servers(FF02::1:2)
link-scoped multicast IPv6 address to request the other configuration 
parameters that it requires.  The Information-Request message is handled either
by a DHCPv6 server on the local link, or forwarded by a DHCPv6 relay agent to a 
DHCPv6 server on a separate network link.  A DHCPv6 relay agent is available in 
most routers and layer 3 switches which support IPv6.

Operational model using a DHCPv6 Relay
Most IPv6 networks will involve one or more IPv6 capable routers.  Most IPv6 
capable routers include an implementation of a DHCPv6 relay agent.  Please 
consult your router vendor's documentation for details on enabling the DHCPv6 
relay agent.
In this operational model, multicast Information-Request messages sent by the
stateless DHCPv6 clients are received by the DHCPv6 relay agent which wraps
the request in a Relay-Forward message.  The DHCPv6 relay agent may send the
wrapped message via unicast to the address of one or more known stateless
DHCPv6 servers, or to the All_DHCP_Servers site-scoped multicast address.
Typically, the DHCPv6 relay agent will be configured with a list of known
DHCPv6 server addresses in order to minimize multicast traffic, and for
security purposes to avoid malicious or accidentally misconfigured servers.
The DHCPv6 server will wrap a Reply for the client in a Relay-Reply message
and unicast the wrapped message to the DHCPv6 relay which forwarded the
original client request.  The relay agent will send the encapsulated Reply
message to the stateless DHCPv6 client via unicast to the source address of
the original client request.
 -----------                  ----------                    -----------
|           |   Multicast    |          |     Unicast*     |           |
|           | -------------> |          | ---------------> |           |
| Stateless |  Info-Request  |  DHCPv6  |  Relay-Forward   | Stateless |
|  DHCPv6   |                |  Relay   |  (Info-Request)  |  DHCPv6   |
|  Client   |                | (Router) |                  |  Server   |
|           |    Unicast     |          |     Unicast      |           |
|           | <------------- |          | <--------------- |           |
|           |     Reply      |          |   Relay-Reply    |           |
 -----------                  ----------      (Reply)       -----------


Operational model without a DHCPv6 Relay
Some IPv6 networks may not involve any IPv6 routers or will otherwise not
utilize a DHCPv6 relay agent.
In this operational model, multicast Information-Request messages sent by the
stateless DHCPv6 clients are received directly by one or more stateless DHCPv6
servers which are configured on the same link as the clients.  The DHCPv6
server(s) will send the Reply message to the source address of the original 
client request.
 -----------                  -----------
|           |   Multicast    |           |
|           | -------------> |           |
| Stateless |  Info-Request  | Stateless |
|  DHCPv6   |                |  DHCPv6   |
|  Client   |                |  Server   |
|           |    Unicast     |           |
|           | <------------- |           |
|           |     Reply      |           |
 -----------                  -----------

 
 Platform Notes
 
 Linux 2.6 - Sun Java 5 Runtime
 - supports unicast mode for operational model with a relay agent
 - supports multicast mode for operational model without a relay agent
 - multithreaded implementation available for unicast mode
 
 Solaris 10 - Sun Java 5 Runtime
 - supports unicast mode for operational model with a relay agent
 - supports multicast mode for operational model without a relay agent
 - multithreaded implementation available for unicast mode
 
 Windows XP - Sun Java 5 Runtime
 - supports unicast mode for operational model with a relay agent
 - multithreaded implementation available for unicast mode
