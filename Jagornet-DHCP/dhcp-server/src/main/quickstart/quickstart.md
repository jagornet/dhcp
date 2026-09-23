# Jagornet-DHCP Quick Start Guide

## 1. Introduction

This guide provides instructions to quickly get the Jagornet-DHCP server up and running from a downloaded binary package. It covers obtaining and extracting the server, initial setup, basic configuration for DHCPv4 and DHCPv6 (stateless), running the server directly or as a service, testing client connectivity, and common troubleshooting steps. The aim is to help users begin serving DHCP requests with minimal effort, while pointing to further resources for more advanced setups.

## Table of Contents (to be expanded)

- [Jagornet-DHCP Quick Start Guide](#jagornet-dhcp-quick-start-guide)
  - [1. Introduction](#1-introduction)
  - [Table of Contents (to be expanded)](#table-of-contents-to-be-expanded)
  - [Prerequisites](#prerequisites)
  - [Obtaining the Server](#obtaining-the-server)
  - [Directory Structure Overview](#directory-structure-overview)
  - [Installation and Initial Setup](#installation-and-initial-setup)
    - [Setting `JAGORNET_DHCP_HOME`](#setting-jagornet_dhcp_home)
    - [Linux/macOS](#linuxmacos)
      - [Running Directly (Foreground)](#running-directly-foreground)
      - [Running as a Systemd Service](#running-as-a-systemd-service)
    - [Windows](#windows)
      - [Running in Console Mode (Foreground)](#running-in-console-mode-foreground)
      - [Running as a Windows Service (using WinSW)](#running-as-a-windows-service-using-winsw)
  - [Configuration](#configuration)
    - [Configuration File Overview](#configuration-file-overview)
    - [Core Configuration Structure (XML Example)](#core-configuration-structure-xml-example)
    - [Configuring a DHCPv4 Link](#configuring-a-dhcpv4-link)
    - [Configuring a DHCPv6 Link (Stateless SLAAC with DNS)](#configuring-a-dhcpv6-link-stateless-slaac-with-dns)
    - [Interface Specification Details](#interface-specification-details)
    - [Logging Configuration](#logging-configuration)
  - [7. Starting the Server and Testing](#7-starting-the-server-and-testing)
    - [A. Starting and Stopping the Server](#a-starting-and-stopping-the-server)
    - [B. Checking Server Status and Logs](#b-checking-server-status-and-logs)
    - [C. Testing with a DHCP Client Machine](#c-testing-with-a-dhcp-client-machine)
    - [D. Using the Bundled Test Clients](#d-using-the-bundled-test-clients)
  - [8. Basic Troubleshooting](#8-basic-troubleshooting)
    - [A. Server Fails to Start](#a-server-fails-to-start)
    - [B. Clients Not Receiving IP Addresses](#b-clients-not-receiving-ip-addresses)
    - [C. Clients Receive Incorrect IP Information](#c-clients-receive-incorrect-ip-information)

## Prerequisites

Before you begin, ensure you have Java Development Kit (JDK) version 11 or newer installed on your system.

To check your Java version, open a terminal or command prompt and run:
```bash
java -version
```
You should see output similar to:
```
openjdk version "11.0.12" 2021-07-20
OpenJDK Runtime Environment (build 11.0.12+7-post-Debian-2)
OpenJDK 64-Bit Server VM (build 11.0.12+7-post-Debian-2, mixed mode, sharing)
```
Ensure the version is 11 or higher.

It's also recommended to have the `JAVA_HOME` environment variable set to the path of your JDK installation. The server scripts may try to use this. You can check if it's set by typing (on Linux/macOS):
```bash
echo $JAVA_HOME
```
Or on Windows:
```cmd
echo %JAVA_HOME%
```
If it's not set, you can set it system-wide (preferred) or within your current terminal session. For example, on Linux/macOS for a session:
```bash
export JAVA_HOME="/path/to/your/jdk-11" # Replace with your actual JDK path
```

## Obtaining the Server

The Jagornet-DHCP server is distributed as a pre-compiled binary package, which you can download and extract.

1.  **Navigate to GitHub Releases:**
    Open your web browser and go to the Jagornet-DHCP GitHub repository: [https://github.com/jagornet/Jagornet-DHCP](https://github.com/jagornet/Jagornet-DHCP).
    Click on the "Releases" link on the right-hand side of the repository page.

2.  **Download the Binary Package:**
    Look for the latest release. In the "Assets" section of the release, you will find distribution packages, typically in `.zip` (for Windows) or `.tar.gz` (for Linux/macOS) format. Download the package appropriate for your operating system.
    For example, `Jagornet-DHCP-server-<version>.zip` or `Jagornet-DHCP-server-<version>.tar.gz`.

3.  **Extract the Archive:**
    Extract the downloaded archive to a location of your choice on your system. For example:
    *   On Linux/macOS:
        ```bash
        tar -xzf Jagornet-DHCP-server-<version>.tar.gz -C /opt/
        # This would extract to /opt/Jagornet-DHCP-server-<version>/
        ```
    *   On Windows:
        Use a tool like 7-Zip or the built-in Windows extractor to extract the contents of the `.zip` file to a folder like `C:\Program Files\`.

4.  **Identify Server Home (`JAGORNET_DHCP_HOME`):**
    The directory created when you extracted the archive is the server's home directory. This directory contains all the necessary files to run the server.
    For instance, if you extracted `Jagornet-DHCP-server-1.2.3.tar.gz` and it created a directory named `Jagornet-DHCP-server-1.2.3`, then this directory (`/opt/Jagornet-DHCP-server-1.2.3` or `C:\Program Files\Jagornet-DHCP-server-1.2.3`) will be referred to as **`$JAGORNET_DHCP_HOME`** (or **`%JAGORNET_DHCP_HOME%`** on Windows) throughout this guide. All scripts and paths are relative to this directory unless specified otherwise.

## Directory Structure Overview

After extracting the binary package, the main directory (`$JAGORNET_DHCP_HOME`) will contain the following key subdirectories and files:

*   `bin/`: Contains executable scripts for running and managing the DHCP server. This includes shell scripts (`.sh`) for Linux/macOS and batch files (`.bat`) for Windows.
*   `config/`: This is where the main configuration file (e.g., `dhcpserver.xml`, `dhcpserver.json`) and other configuration-related files are located.
    *   `config/samples/`: Contains sample configuration files. You should copy one from here to the `config/` directory to get started.
    *   `config/schema/`: Contains XML schema definition files (`.xsd`) for validating XML configurations.
    *   The server will create/update lease files (e.g., `dhcpd.leases`) in this directory once it's running and serving clients.
*   `lib/`: Contains the compiled Java Archive (JAR) files for the server and all its dependencies. Since you downloaded a binary package, this directory is already populated with all necessary JARs.
*   `docs/`: Contains documentation, including this quick start guide, license information, and potentially other relevant documents.
*   `bin/jagornet-dhcp.service`: Systemd service unit file for Linux service installation.
*   `bin/jagornet-service.xml`: WinSW configuration file for Windows service installation.
*   `logs/`: This directory will be created automatically when the server runs for the first time. It will contain log files generated by the server (e.g., `JagornetDhcpServer.log`).
*   `LICENSE` and `NOTICE` files: Contain licensing information for the server and its dependencies.

## Installation and Initial Setup

No traditional installation (like running an installer program) is required. Once the server package is extracted, it can be run directly from the `$JAGORNET_DHCP_HOME` directory.

### Setting `JAGORNET_DHCP_HOME`

The server scripts rely on the `JAGORNET_DHCP_HOME` environment variable to locate necessary files (libraries, configuration, etc.).

*   **Automatic Detection:** Most scripts in the `bin/` directory attempt to automatically determine `JAGORNET_DHCP_HOME` based on their own location. This usually works if you run them directly from the `bin/` directory or if your current working directory is `$JAGORNET_DHCP_HOME`.
*   **Manual Setting (Recommended for Services):** For running the server as a service or for more robust setups, it's highly recommended to explicitly set `JAGORNET_DHCP_HOME`.
    *   **Linux/macOS:**
        ```bash
        export JAGORNET_DHCP_HOME="/path/to/your/Jagornet-DHCP-server-<version>"
        # Example: export JAGORNET_DHCP_HOME="/opt/Jagornet-DHCP-server-1.2.3"
        # Add this line to your ~/.bashrc, ~/.zshrc, or /etc/environment for persistence.
        ```
    *   **Windows:**
        ```cmd
        set JAGORNET_DHCP_HOME="C:\path\to\your\Jagornet-DHCP-server-<version>"
        REM Example: set JAGORNET_DHCP_HOME="C:\Program Files\Jagornet-DHCP-server-1.2.3"
        REM To set it permanently, use System Properties -> Environment Variables.
        ```
    Replace the example path with the actual path to your extracted server directory (your `$JAGORNET_DHCP_HOME`).

### Linux/macOS

**Important: Script Permissions**

Files extracted from `.tar.gz` archives on Linux or macOS might not have execute permissions set. Before running any scripts, you may need to make them executable.

Navigate to your `$JAGORNET_DHCP_HOME` directory and run:
```bash
chmod +x ./bin/dhcpserver ./bin/testclientv4 ./bin/testclientv6 ./bin/gentestconfig
```

#### Running Directly (Foreground)

This method is useful for testing, debugging, or interactive use. The server runs directly in your current terminal session.

Navigate to the server home directory (`$JAGORNET_DHCP_HOME`):
```bash
cd /path/to/your/Jagornet-DHCP-server-<version>
# Example: cd /opt/Jagornet-DHCP-server-1.2.3
```

Available commands using the `dhcpserver` script:

*   **Display help / command options (instant):**
    ```bash
    ./bin/dhcpserver --help
    ```

*   **Start the server (with default configuration `config/dhcpserver.xml`):**
    ```bash
    ./bin/dhcpserver
    ```
    To stop the server when running in the foreground, press **`Ctrl+C`**.

*   **Start the server with a specific configuration file:**
    ```bash
    ./bin/dhcpserver -c /path/to/your/custom_dhcpd.conf
    # or relative to server home:
    ./bin/dhcpserver -c config/alternative_dhcpd.conf
    ```

*   **Show server version:**
    ```bash
    ./bin/dhcpserver -v
    ```

*   **List available network interfaces:**
    Helpful for identifying interface names for the configuration file:
    ```bash
    ./bin/dhcpserver --list-interfaces
    ```

*   **Test configuration file syntax:**
    ```bash
    ./bin/dhcpserver --test-configfile -c config/my_custom_config.xml
    ```

#### Running as a Systemd Service

On modern Linux distributions, the recommended way to run Jagornet-DHCP as a daemon is with **systemd**. A pre-configured service unit file is provided at `bin/jagornet-dhcp.service`.

1.  **Copy the unit file to systemd:**
    ```bash
    sudo cp ./bin/jagornet-dhcp.service /etc/systemd/system/
    ```

2.  **Adjust service settings (if needed):**
    Open `/etc/systemd/system/jagornet-dhcp.service` in an editor to verify paths (`WorkingDirectory` and `ExecStart`), or configure environment overrides in `/etc/default/jagornet-dhcp`.
    
    *Note on Security:* The unit file specifies `AmbientCapabilities=CAP_NET_BIND_SERVICE CAP_NET_RAW`, which allows the server to bind to DHCP ports 67/547 while running as an unprivileged user (e.g., `User=jagornet`).

3.  **Reload systemd and enable the service:**
    ```bash
    sudo systemctl daemon-reload
    sudo systemctl enable --now jagornet-dhcp
    ```

4.  **Check service status:**
    ```bash
    sudo systemctl status jagornet-dhcp
    ```

5.  **View live logs:**
    ```bash
    sudo journalctl -u jagornet-dhcp -f
    ```

6.  **Stop or restart the service:**
    ```bash
    sudo systemctl stop jagornet-dhcp
    sudo systemctl restart jagornet-dhcp
    ```

### Windows

#### Running in Console Mode (Foreground)

Similar to running directly on Linux/macOS, this runs the server in the current Command Prompt window.

1.  **Open a Command Prompt** (`cmd.exe`).
2.  **Navigate to the server home directory:**
    ```cmd
    cd C:\path\to\your\Jagornet-DHCP-server-<version>
    REM Example: cd C:\Program Files\Jagornet-DHCP-server-1.2.3
    ```
3.  **Run the server:**
    The primary script is `bin\dhcpserver.bat`.
    *   **Display help / command options (instant):**
        ```cmd
        bin\dhcpserver.bat --help
        ```
    *   **Start the server (with default configuration `config\dhcpserver.xml`):**
        ```cmd
        bin\dhcpserver.bat
        ```
    *   **Start with a specific configuration file:**
        ```cmd
        bin\dhcpserver.bat -c C:\path\to\your\custom_dhcpd.conf
        ```
    *   **Stop the server:**
        Press **`Ctrl+C`** in the console window.
    *   **Show server version:**
        ```cmd
        bin\dhcpserver.bat -v
        ```
    *   **List available network interfaces:**
        ```cmd
        bin\dhcpserver.bat --list-interfaces
        ```
    *   **Test configuration file syntax:**
        ```cmd
        bin\dhcpserver.bat --test-configfile -c config\my_custom_config.xml
        ```

#### Running as a Windows Service (using WinSW)

To run Jagornet-DHCP as a background Windows Service, [WinSW (Windows Service Wrapper)](https://github.com/winsw/winsw) is pre-bundled as `bin\jagornet-service.exe` with its configuration `bin\jagornet-service.xml`.

1.  **Install the service:**
    Open Command Prompt as Administrator and run:
    ```cmd
    bin\winsw-install.bat
    ```
    This registers "Jagornet DHCP Server" in Windows Services and starts it automatically.
2.  **Manage the service:**
    *   **Start:** `bin\jagornet-service.exe start` (or via Windows Services `services.msc`)
    *   **Stop:** `bin\jagornet-service.exe stop`
    *   **Status:** `bin\jagornet-service.exe status`
3.  **Uninstall the service:**
    ```cmd
    bin\winsw-uninstall.bat
    ```

Logs for the service are located in `%JAGORNET_DHCP_HOME%\logs\jagornet-service.log` and `%JAGORNET_DHCP_HOME%\logs\JagornetDhcpServer.log`.

## Configuration

This section details how to configure the Jagornet-DHCP server.

### Configuration File Overview

*   **Location:** Configuration files are located in the `$JAGORNET_DHCP_HOME/config/` directory.
*   **Supported Formats:** Jagornet-DHCP supports configuration files in XML, JSON, and YAML formats. This guide will primarily use XML for examples due to its explicit structure, but JSON is equally well-supported. Sample configuration files for all supported formats are typically provided in the `config/samples/` directory.
*   **Default File Loading:**
    *   When the server starts without a specific configuration file specified (e.g., `./bin/dhcpserver`), it looks for a default configuration file in the `config/` directory.
    *   The server searches for files in the following order of preference:
        1.  `dhcpserver.xml`
        2.  `dhcpserver.json`
        3.  `dhcpserver.yaml` (or `dhcpserver.yml`)
*   **Recommendation:** It's highly recommended to copy one of the sample configuration files from `config/samples/` to the `config/` directory and rename it to one of the default names. Then, modify this copy.
    For example, to start with a basic XML configuration:
    ```bash
    # Assuming you are in $JAGORNET_DHCP_HOME
    cp config/samples/dhcpserver-basic.xml config/dhcpserver.xml
    ```
    Or for JSON:
    ```bash
    cp config/samples/dhcpserver-basic.json config/dhcpserver.json
    ```
*   **Testing Configuration:** Before starting the server with a new or modified configuration, always test its syntax:
    ```bash
    # For the default configuration file (e.g., config/dhcpserver.xml)
    ./bin/dhcpserver test-configfile

    # For a specific configuration file
    ./bin/dhcpserver test-configfile -c config/my_custom_config.xml
    ```
    This command will parse the file and report any syntax errors or structural issues.

### Core Configuration Structure (XML Example)

The main configuration is enclosed within the `<dhcpServerConfig>` element. Here's a simplified skeleton of a `dhcpserver.xml` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dhcpServerConfig
    xmlns="http://www.jagornet.com/dhcp/xml/schema/dhcpServerConfig-1.2"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.jagornet.com/dhcp/xml/schema/dhcpServerConfig-1.2 dhcpServerConfig-1.2.xsd">

    <!--
        Server ID options: Unique identifiers for your DHCP server.
        These are typically derived from a MAC address or can be set manually.
        For testing, placeholder values are often fine.
    -->
    <v4ServerIdOption>
        <!-- Example: type DUID_LLT, LLT = Link-Layer address Plus Time -->
        <type>DUID_LLT</type>
        <lltTime>2023-01-01T00:00:00Z</lltTime> <!-- Set a fixed time -->
        <linkLayerAddr>00:11:22:33:44:55</linkLayerAddr> <!-- Replace with a MAC from your server -->
    </v4ServerIdOption>

    <v6ServerIdOption>
        <!-- Example: type DUID_LL, LL = Link-Layer address -->
        <type>DUID_LL</type>
        <linkLayerAddr>00:11:22:33:44:55</linkLayerAddr> <!-- Replace with a MAC from your server -->
    </v6ServerIdOption>

    <!--
        Global policies that apply to all links unless overridden at the link level.
    -->
    <policies>
        <v4Policies>
            <defaultLeaseTimeSecs>3600</defaultLeaseTimeSecs> <!-- Default DHCPv4 lease: 1 hour -->
            <maxLeaseTimeSecs>7200</maxLeaseTimeSecs>       <!-- Max DHCPv4 lease: 2 hours -->
        </v4Policies>
        <v6Policies>
            <defaultPreferredLifetimeSecs>3600</defaultPreferredLifetimeSecs> <!-- Default v6 preferred life: 1 hour -->
            <defaultValidLifetimeSecs>7200</defaultValidLifetimeSecs>       <!-- Default v6 valid life: 2 hours -->
        </v6Policies>
    </policies>

    <!--
        Global DHCPv6 message configuration options.
        These can be overridden at the link level.
        Example: Providing DNS servers for all DHCPv6 clients by default.
    -->
    <v6MsgConfigOptions>
        <dnsServers>
            <server>2001:4860:4860::8888</server> <!-- Google Public DNS IPv6 -->
            <server>2001:4860:4860::8844</server>
        </dnsServers>
        <domainSearchList>
            <domain>example.com</domain>
            <domain>ipv6.example.com</domain>
        </domainSearchList>
    </v6MsgConfigOptions>

    <!--
        The main container for defining network links (subnets or interfaces)
        that the DHCP server will manage.
    -->
    <linkList>
        <!-- DHCPv4 and DHCPv6 link configurations will go here -->
        <!-- See examples below -->
    </linkList>

</dhcpServerConfig>
```
**Note:** The `xmlns` (XML namespace) and `xsi:schemaLocation` attributes are important for XML validation if you use an XML editor that supports it. The schema file (e.g., `dhcpServerConfig-1.2.xsd`) is usually found in the `config/schema/` directory. A JSON equivalent would have a similar nested structure without the XML-specific attributes.

### Configuring a DHCPv4 Link

A "link" in Jagornet-DHCP configuration typically refers to a specific network segment (subnet) the server is responsible for.

Here's an example of configuring a single IPv4 link for the `192.168.1.0/24` network, added within the `<linkList>` tags from the core structure above:

```xml
    <linkList>
        <v4Link>
            <!--
                The IPv4 subnet address and prefix length (CIDR).
                The server will listen on local network interfaces that have an IP address
                within this subnet.
            -->
            <address>192.168.1.0/24</address>

            <!-- Link-specific policies can override global policies -->
            <policies>
                <defaultLeaseTimeSecs>1800</defaultLeaseTimeSecs> <!-- 30 minutes for this specific link -->
                <maxLeaseTimeSecs>3600</maxLeaseTimeSecs>       <!-- 1 hour for this specific link -->
            </policies>

            <!--
                DHCPv4 options to provide to clients on this link.
            -->
            <v4ConfigOptions>
                <subnetMask>255.255.255.0</subnetMask>
                <routers>
                    <router>192.168.1.1</router> <!-- Your gateway/router IP on this subnet -->
                </routers>
                <dnsServers>
                    <server>192.168.1.1</server> <!-- Can be the router itself or another DNS server -->
                    <server>8.8.8.8</server>     <!-- Google Public DNS as a secondary -->
                </dnsServers>
                <domainName>mynetwork.local</domainName>
            </v4ConfigOptions>

            <!--
                Address pools from which to assign dynamic IP addresses.
                Multiple pools can be defined.
            -->
            <v4AddrPools>
                <pool>
                    <rangeLo>192.168.1.100</rangeLo>
                    <rangeHi>192.168.1.200</rangeHi>
                </pool>
            </v4AddrPools>

            <!--
                Optional: Static IP address assignments (reservations) based on MAC address.
            -->
            <v4AddrBindings>
                <binding>
                    <chaddr>00:AA:BB:CC:DD:EE</chaddr> <!-- Client's Ethernet MAC address -->
                    <ipAddr>192.168.1.50</ipAddr>
                    <hostname>fileserver.mynetwork.local</hostname> <!-- Optional hostname to register in DNS -->
                </binding>
                <!-- Add more <binding> elements for other static assignments -->
            </v4AddrBindings>
        </v4Link>
        <!-- You can add more <v4Link> or <v6Link> elements here for other networks -->
    </linkList>
```

**How it matches the interface (IPv4):**
For IPv4, the server automatically identifies the local network interface(s) to use by finding which of the machine's configured interfaces have an IP address that falls within the defined `<address>` subnet (e.g., `192.168.1.0/24`). You don't explicitly name the interface for IPv4 links in the configuration; the server listens on all interfaces that match a configured subnet.

### Configuring a DHCPv6 Link (Stateless SLAAC with DNS)

This example sets up a DHCPv6 link to provide DNS servers and a domain search list to clients. These clients are expected to use SLAAC (Stateless Address Autoconfiguration) for their IPv6 addresses, and this DHCPv6 configuration provides them with "other" information (like DNS).

Add this within the `<linkList>`:

```xml
    <linkList>
        <!-- ... (previous v4Link example if any) ... -->

        <v6Link>
            <!--
                For DHCPv6, you MUST explicitly specify the network interface
                the server should listen on for this link.
                Replace 'eth0' with your actual LAN-facing interface name.
                Use './bin/dhcpserver list-interfaces' to find available names.
            -->
            <interface>eth0</interface>

            <!--
                Link-specific policies for preferred and valid lifetimes for options.
                These are less critical for stateless DNS/option provisioning but good practice.
            -->
            <policies>
                <defaultPreferredLifetimeSecs>7200</defaultPreferredLifetimeSecs> <!-- 2 hours -->
                <defaultValidLifetimeSecs>14400</defaultValidLifetimeSecs>     <!-- 4 hours -->
            </policies>

            <!--
                DHCPv6 message configuration options for this link.
                This will override any global v6MsgConfigOptions for this specific link.
            -->
            <v6MsgConfigOptions>
                <dnsServers>
                    <!-- Provide IPv6 DNS servers -->
                    <server>2001:4860:4860::8888</server> <!-- Google Public DNS IPv6 -->
                    <server>2606:4700:4700::1111</server> <!-- Cloudflare DNS IPv6 -->
                </dnsServers>
                <domainSearchList>
                    <domain>ipv6.mynetwork.local</domain>
                    <domain>mynetwork.local</domain>
                </domainSearchList>
                <!-- You can also advertise SNTP servers, etc., here -->
            </v6MsgConfigOptions>

            <!--
                For a purely stateless DHCPv6 setup (providing only options like DNS),
                you do NOT define <v6NaAddrPools> (Non-temporary Address Pools) or
                <v6TaAddrPools> (Temporary Address Pools).
                The server will automatically set the O-flag (Other Configuration) in its
                Router Advertisements (if it's also configured to send RAs, or it will respond
                to Information-Requests from clients prompted by another router's RAs).

                If you wanted to provide stateful DHCPv6 address assignment on this link,
                you would add <v6NaAddrPools> here. For example:
            -->
            <!--
            <v6NaAddrPools>
                <pool>
                    <prefix>2001:db8:cafe:1::/64</prefix> <!- Must match your network's prefix ->
                    <rangeLo>2001:db8:cafe:1::100</rangeLo>
                    <rangeHi>2001:db8:cafe:1::200</rangeHi>
                </pool>
            </v6NaAddrPools>
            -->
        </v6Link>
    </linkList>
```

**Explanation for Stateless DHCPv6:**
In this stateless setup:
1.  Your network's router (which could be another device, or Jagornet-DHCP itself if configured for Router Advertisements) advertises an IPv6 prefix. Clients use this prefix for SLAAC to configure their own IPv6 addresses.
2.  The router's RAs should set the "O" (Other configuration) flag, signaling clients to seek additional information via DHCPv6.
3.  Jagornet-DHCP, listening on the specified `<interface>` (e.g., `eth0`), responds to these DHCPv6 Information-Request messages from clients.
4.  It provides the options defined in `<v6MsgConfigOptions>` (like DNS servers and domain search list).
5.  It does **not** assign IPv6 addresses from a pool in this stateless configuration. For stateful address assignment (where Jagornet-DHCP assigns specific IPv6 addresses), you would define `<v6NaAddrPools>` and ensure RAs have the "M" (Managed address configuration) flag set.

### Interface Specification Details

*   **IPv4:** The server determines which local network interface(s) to listen on by comparing the IP addresses configured on the host's interfaces against the subnets defined in `<v4Link><address>` elements. If a host interface has an IP like `192.168.1.10`, it will automatically serve the `192.168.1.0/24` link. You do not specify interface names for IPv4 links.
*   **IPv6:** For DHCPv6, you **must** explicitly specify the network interface using the `<v6Link><interface>eth0</interface>` element (replace `eth0` with the actual interface name). The server will bind to this named interface for DHCPv6 traffic. This is because IPv6 interfaces typically have multiple addresses (e.g., link-local, global), and explicit binding is necessary for correct operation.
*   **Listing Interfaces:** To see a list of available network interface names on your system that Jagornet-DHCP can recognize, use the command:
    ```bash
    # In your $JAGORNET_DHCP_HOME directory
    ./bin/dhcpserver list-interfaces
    ```
    Use the names from this output (e.g., `eth0`, `en1`, `net0`, `Ethernet 2`) in your `<v6Link><interface>` configuration.

### Logging Configuration

*   **Application Logs:** Jagornet-DHCP uses Log4j 2 for its internal application logging. The primary configuration for Log4j 2 is found in `$JAGORNET_DHCP_HOME/config/log4j2.xml`. You can modify this file to change log levels (e.g., DEBUG, INFO, ERROR), output destinations (e.g., console, file), and log rotation policies.
    *   Default application log file location: `$JAGORNET_DHCP_HOME/logs/JagornetDhcpServer.log`
*   **Service Wrapper Logs (when running as a service):**
    *   **Linux (systemd):** Captured automatically by the systemd journal (`sudo journalctl -u jagornet-dhcp -f`).
    *   **Windows (WinSW):** WinSW logs and console redirects are written to `%JAGORNET_DHCP_HOME%\logs\jagornet-service.log` and related log files in the `logs\` directory.

Always restart the Jagornet-DHCP server after making changes to its main configuration file (e.g., `dhcpserver.xml`) or its logging configuration (`log4j2.xml`) for the changes to take effect.


## 7. Starting the Server and Testing

Once you have configured your `dhcpserver.xml` (or equivalent JSON/YAML) file, you are ready to start the server and test its operation.

### A. Starting and Stopping the Server

Refer to the 'Installation and Initial Setup' section for detailed instructions. Here's a quick recap:

**Starting the Server:**

*   **Linux/macOS:**
    *   **Directly (Foreground):**
        Navigate to `$JAGORNET_DHCP_HOME` and run:
        ```bash
        sudo ./bin/dhcpserver
        # Use 'sudo' if server needs to listen on privileged ports (like 67, 547)
        # or if configured to use low-numbered ports.
        ```
    *   **As a Systemd Service:**
        ```bash
        sudo systemctl start jagornet-dhcp
        ```
*   **Windows:**
    *   **Console Mode (Foreground):**
        Navigate to `%JAGORNET_DHCP_HOME%` and run:
        ```cmd
        bin\dhcpserver.bat
        ```
    *   **As a Windows Service (WinSW):**
        ```cmd
        bin\jagornet-service.exe start
        ```
        Or, start "Jagornet DHCP Server" from the Windows Services console (`services.msc`).

**Stopping the Server:**

*   **Linux/macOS:**
    *   **Directly:** Press `Ctrl+C` in the terminal session.
    *   **Systemd Service:**
        ```bash
        sudo systemctl stop jagornet-dhcp
        ```
*   **Windows:**
    *   **Console Mode:** Press `Ctrl+C` in the Command Prompt window.
    *   **Windows Service:**
        ```cmd
        bin\jagornet-service.exe stop
        ```
        Or, stop "Jagornet DHCP Server" from the Windows Services console.

### B. Checking Server Status and Logs

**Checking Status:**

*   **Linux/macOS:**
    *   **Systemd Service:**
        ```bash
        sudo systemctl status jagornet-dhcp
        ```
*   **Windows:**
    *   **Windows Service:**
        ```cmd
        bin\jagornet-service.exe status
        ```
        Or check the status in the Windows Services console (`services.msc`).

**Log File Locations:**

If the server doesn't start or you encounter unexpected behavior, the log files are the first place to look.

*   **Application Logs:**
    *   Configuration: Controlled by `$JAGORNET_DHCP_HOME/config/log4j2.xml`.
    *   Typical Location: `$JAGORNET_DHCP_HOME/logs/JagornetDhcpServer.log`
    *   These logs contain detailed information about DHCP operations, errors in processing packets, configuration issues, etc.

*   **Systemd Journal (Linux Service):**
    *   View live logs: `sudo journalctl -u jagornet-dhcp -f`
    *   View recent failures: `sudo journalctl -u jagornet-dhcp -xe`

*   **WinSW Service Logs (Windows Service):**
    *   Typical Location: `%JAGORNET_DHCP_HOME%\logs\jagornet-service.log`
    *   These logs show JVM process startup, stop, and wrapper status.

**Always check these logs for error messages or clues if something is not working as expected.**

### C. Testing with a DHCP Client Machine

The most effective way to test your DHCP server is to use an actual client machine. This can be another physical computer or a virtual machine (VM) on the same network segment as the interface Jagornet-DHCP is configured to listen on.

**General Steps:**

1.  **Network Connection:** Ensure the client machine is physically or virtually connected to the same network (LAN segment or VLAN) that your Jagornet-DHCP server is serving. For example, if your server is configured for `192.168.1.0/24` on `eth0`, the client machine should be connected to the network switch that `eth0` is connected to.
2.  **Client Network Configuration:**
    *   **IPv4:** On the client machine, go to its network adapter settings and configure it to "Obtain an IP address automatically."
    *   **IPv6:** Similarly, configure it to "Obtain IPv6 address automatically." This typically enables SLAAC and allows the client to listen for DHCPv6 information.
3.  **Renew IP Address (Optional):**
    If the client previously had a static IP or an old DHCP lease, you might need to force it to request a new one.
    *   **Windows:** Open Command Prompt and run `ipconfig /release` then `ipconfig /renew`. For IPv6, `ipconfig /release6` and `ipconfig /renew6`.
    *   **Linux:** `sudo dhclient -r` (to release) then `sudo dhclient` (to renew). Or restart the networking service: `sudo systemctl restart networking` or `sudo systemctl restart NetworkManager`.
    *   **macOS:** Go to System Settings > Network, select the interface, click "Details...", then TCP/IP, and click "Renew DHCP Lease."

**Verification on the Client:**

After the client attempts to get an IP address:

1.  **Check IP Address:**
    *   **Windows:** `ipconfig /all`
    *   **Linux:** `ip addr show` or `ifconfig`
    *   **macOS:** `ifconfig` or check System Settings > Network.
    Verify if the client received an IP address from the range you configured in `dhcpserver.xml`. For IPv6, check if it has a global unicast address (if using SLAAC from a prefix advertised by your router or Jagornet-DHCP if it's doing RAs) and/or a temporary address.
2.  **Verify Gateway and DNS:**
    Check if the Default Gateway (Router) and DNS Server addresses assigned to the client match what you configured in your DHCP server's options for that link.
3.  **Test Connectivity:**
    *   Try to `ping` the gateway IP address.
    *   Try to resolve an internet domain name (e.g., `ping www.google.com`) to check if DNS is working.
    *   Access a website in a browser.

**Check Server Logs:**
Simultaneously, monitor the `$JAGORNET_DHCP_HOME/logs/JagornetDhcpServer.log` file on your DHCP server. You should see messages indicating DHCPDISCOVER, DHCPOFFER, DHCPREQUEST, and DHCPACK (for IPv4) or Solicit, Advertise, Request, Reply (for DHCPv6) messages being processed for the client's MAC address.

### D. Using the Bundled Test Clients

Jagornet-DHCP includes command-line test clients that can simulate basic DHCP client requests. These are useful for initial checks without needing a separate client machine or for scripting tests.

The scripts are located in the `$JAGORNET_DHCP_HOME/bin/` directory.

*   **Linux/macOS:**
    Make sure the scripts are executable (`chmod +x ./bin/dhcpserver ./bin/testclientv4 ./bin/testclientv6 ./bin/gentestconfig`).
    *   **DHCPv4 Test Client:**
        ```bash
        cd $JAGORNET_DHCP_HOME
        ./bin/testclientv4 [options]
        # For help on options:
        ./bin/testclientv4 -? 
        ```
        Example: To send a discover from a specific interface:
        `./bin/testclientv4 -i eth0`
    *   **DHCPv6 Test Client:**
        ```bash
        cd $JAGORNET_DHCP_HOME
        ./bin/testclientv6 [options]
        # For help on options:
        ./bin/testclientv6 -?
        ```
        Example: To solicit for options on a specific interface:
        `./bin/testclientv6 -i eth0 -information-only SOLICIT`

*   **Windows:**
    *   **DHCPv4 Test Client:**
        ```cmd
        cd %JAGORNET_DHCP_HOME%
        bin\testclientv4.bat [options]
        REM For help on options:
        bin\testclientv4.bat -?
        ```
        Example:
        `bin\testclientv4.bat -i "Ethernet 2"` (use interface name from `--list-interfaces`)
    *   **DHCPv6 Test Client:**
        ```cmd
        cd %JAGORNET_DHCP_HOME%
        bin\testclientv6.bat [options]
        REM For help on options:
        bin\testclientv6.bat -?
        ```
        Example:
        `bin\testclientv6.bat -i "Ethernet 2" -information-only SOLICIT`

These test clients will output the DHCP server's response, allowing you to verify if the server is responding and what options it's providing. They are particularly useful for checking if the server is reachable on the correct interface and if basic options are being returned as configured.
Remember to consult the output of `./bin/dhcpserver --list-interfaces` (or `bin\dhcpserver.bat --list-interfaces` on Windows) to get the correct interface names for the `-i` option if needed by the test clients.
```

## 8. Basic Troubleshooting

This section provides guidance on diagnosing common issues.

### A. Server Fails to Start

If the Jagornet-DHCP server does not start, here are common areas to investigate:

1.  **Check Java Installation:**
    *   **Verify Version:** Ensure you have Java Development Kit (JDK) version 11 or newer. Run `java -version`.
    *   **Check PATH/JAVA_HOME:** Make sure the `java` executable is in your system's PATH, or that the `JAVA_HOME` environment variable is correctly set to your JDK installation directory. The server scripts often rely on these.

2.  **Review Logs (Crucial First Step):**
    *   **Application Logs:** Check `$JAGORNET_DHCP_HOME/logs/JagornetDhcpServer.log`. Look for Java stack traces or error messages that indicate why the application itself failed to initialize or run.
    *   **Service Wrapper Logs:** If running as a service, check service logs:
        *   Linux (systemd): `sudo journalctl -u jagornet-dhcp -e`
        *   Windows (WinSW): `%JAGORNET_DHCP_HOME%\logs\jagornet-service.log`

3.  **Configuration File Errors:**
    *   **Validate Syntax:** Use the `--test-configfile` option to check your configuration file for syntax errors. Navigate to `$JAGORNET_DHCP_HOME` and run:
        ```bash
        # For Linux/macOS, assuming your config is $JAGORNET_DHCP_HOME/config/dhcpserver.xml
        ./bin/dhcpserver --test-configfile -c config/dhcpserver.xml 
        ```
        ```cmd
        REM For Windows, assuming your config is %JAGORNET_DHCP_HOME%\config\dhcpserver.xml
        bin\dhcpserver.bat --test-configfile -c config\dhcpserver.xml
        ```
        If your configuration file is located elsewhere or named differently, adjust the path accordingly (e.g., `../config/dhcpserver.xml` if you are in the `bin` directory, or an absolute path).
    *   **Correct File Loaded:** Ensure the server is attempting to load the configuration file you intend to use. If you start the server without the `-c` option, it looks for default names like `dhcpserver.xml`, `dhcpserver.json`, or `dhcpserver.yaml` in the `$JAGORNET_DHCP_HOME/config/` directory.

4.  **Port Conflicts:**
    *   **Required Ports:** DHCP servers need exclusive access to specific UDP ports:
        *   DHCPv4: Port 67 (server) and Port 68 (client messages often originate from here)
        *   DHCPv6: Port 547 (server) and Port 546 (client messages often originate from here)
    *   **Check Logs for Errors:** Look for messages like "Address already in use," "bind failed," "Permission denied" (especially on Linux/macOS for ports below 1024 if not running as root or with sufficient privileges), or similar in `JagornetDhcpServer.log`.
    *   **Identify Conflicting Applications:** Ensure no other DHCP server (like another instance of Jagornet-DHCP, `dnsmasq`, ISC DHCPD, or a router's built-in DHCP server if on the same machine) or other application is already using these ports.
        *   **Linux/macOS:** Use `sudo netstat -tulnp | grep -E ':(67|68|546|547)'` or `sudo ss -tulnp | grep -E ':(67|68|546|547)'`.
        *   **Windows:** Use `netstat -ano -p udp | findstr ":67"` and similarly for other ports. Then use Task Manager (Details tab, find PID from `netstat` output) or `tasklist /svc /FI "PID eq <PID>"` to find the process owning the port.
    *   **Privileged Ports (Linux/macOS):** Ports below 1024 are privileged. If you try to run `./bin/dhcpserver` directly without `sudo`, it will fail to bind to these ports. Using `sudo` or running as a systemd service (which is configured with `CAP_NET_BIND_SERVICE`) is required.

5.  **Permissions (Linux/macOS):**
    *   **Execute Permissions:** Ensure the server scripts have execute permissions (e.g., `chmod +x ./bin/dhcpserver ./bin/testclientv4 ./bin/testclientv6 ./bin/gentestconfig`). This was covered in the "Important: Script Permissions" note earlier.
    *   **Privileged Ports:** As mentioned above, direct execution for privileged ports (like 67, 547) requires `sudo`. Service installations via systemd or WinSW handle the necessary privileges.

### B. Clients Not Receiving IP Addresses

If clients are not getting IP addresses or DHCP configuration:

1.  **Verify Server Status:**
    *   Confirm that the Jagornet-DHCP server process is actually running. Use the status commands outlined in "Section 7B: Checking Server Status and Logs."

2.  **Check Server Logs:**
    *   Examine `$JAGORNET_DHCP_HOME/logs/JagornetDhcpServer.log` for any activity related to the client's MAC address or DUID.
    *   Look for incoming DHCPDISCOVER (v4) or SOLICIT (v6) messages.
    *   Note any error messages related to processing these requests, selecting a subnet/link, or allocating an address from a pool (e.g., "No address available," "No link configured for...").
    *   Increase log verbosity in `$JAGORNET_DHCP_HOME/config/log4j2.xml` (e.g., to DEBUG level for relevant loggers like `com.jagornet.dhcp`) if more detail is needed, then restart the server.

3.  **Client Network Configuration:**
    *   **Automatic IP:** Double-check that the client machine's network adapter is configured to "Obtain an IP address automatically" (for IPv4) and "Obtain IPv6 address automatically" (for IPv6).
    *   **Correct Network Segment:** Ensure the client is on the same physical or virtual network segment (LAN/VLAN) that the Jagornet-DHCP server is configured to listen on for that client's expected subnet/link.

4.  **Network Connectivity:**
    *   **Basic Checks:** Verify physical network connections (cables plugged in, Wi-Fi connected to the correct network).
    *   **Switches/Routers:** If there are managed switches or routers between the client and server, ensure they are not blocking DHCP traffic (e.g., DHCP snooping configured incorrectly, VLAN misconfiguration).
    *   **DHCP Relay:** If the client is on a different subnet from the server, ensure a DHCP relay agent (often called an "IP helper address" on routers) is correctly configured on the client's gateway router to forward DHCP requests to the Jagornet-DHCP server's IP address.

5.  **Firewall Issues:**
    *   **Server-Side Firewall:** The firewall on the machine running Jagornet-DHCP must allow incoming UDP traffic on:
        *   Port 67 and 68 for DHCPv4.
        *   Port 547 and 546 for DHCPv6.
        Check `ufw` (Linux), `firewalld` (Linux), or Windows Firewall rules.
    *   **Client-Side Firewall:** While less common for DHCP, a very restrictive client-side firewall could potentially block outgoing DHCP requests or incoming offers. This is usually not an issue with default client OS firewall settings.

6.  **Scope/Pool Configuration (Server-Side):**
    *   **Link Matching:** Verify that the client's requests are matching a configured `<link>` in the server's `dhcpserver.xml`.
        *   For IPv4, if requests come directly from clients, the server matches by finding a `<v4Link>` whose `<address>` subnet contains an IP address configured on one of the server's local interfaces. If requests come from a relay, the `giaddr` (gateway IP address in the DHCP packet) is used to find a matching link.
        *   For IPv6, ensure the `<interface>` specified in the `<v6Link>` is correct, active, and the one the client's requests are arriving on. Use `./bin/dhcpserver list-interfaces` to verify available interface names.
    *   **Address Pools:**
        *   Ensure `<v4AddrPools>` (for IPv4) or `<v6NaAddrPools>` (for stateful DHCPv6) are defined for the relevant link.
        *   Check that these pools have available IP addresses. The log file might indicate if a pool is exhausted.
    *   **Lease File:** Check the lease file (e.g., `$JAGORNET_DHCP_HOME/config/dhcpd.leases` or as configured) to see if leases are being recorded or if there are unexpected entries.

### C. Clients Receive Incorrect IP Information

If clients get an IP address but other details (like DNS servers, router, domain name) are wrong:

1.  **Review Server Configuration:**
    *   **Correct Link:** Carefully examine the `<v4ConfigOptions>` (for DHCPv4) or `<v6MsgConfigOptions>` (for DHCPv6) within the specific `<v4Link>` or `<v6Link>` that is serving the client.
    *   **Option Values:** Double-check the IP addresses for routers, DNS servers, NTP servers, etc., and any domain names or lease times defined. Typos are a common source of errors.
    *   **Global vs. Link-Specific:** Remember that options can be defined globally (e.g., in `<policies>` or global `<v6MsgConfigOptions>`) and can be overridden at the link level. Ensure the configuration you are checking is the one being applied to the client. The most specific configuration usually wins.
2.  **Editing the Correct File:**
    *   Confirm you are editing the exact configuration file that the Jagornet-DHCP server is loading. If you made changes to a backup or a differently named file, the server won't see them unless you explicitly tell it to load that file (using the `-c` option at startup).
    *   Restart the server after any configuration changes to ensure they are applied.
3.  **Old Leases / Other DHCP Servers:**
    *   The client might be holding onto an old lease from a previous configuration or from a different DHCP server on the network. Try releasing and renewing the lease on the client (see Section 7C for client-side commands).
    *   **Rogue DHCP Servers:** Ensure no other DHCP server on the network (e.g., a misconfigured router, another test server) is unintentionally providing configuration to clients. This can lead to conflicting information and unpredictable behavior. Network monitoring tools (like Wireshark/tcpdump filtering for DHCP traffic) can help identify other DHCP offers.

If problems persist, consider increasing the logging level in `$JAGORNET_DHCP_HOME/config/log4j2.xml` to `DEBUG` or even `TRACE` for relevant Jagornet-DHCP components (e.g., `com.jagornet.dhcp.server`, `com.jagornet.dhcp.config`). This provides more detailed insight into how the server is processing requests and options. Remember to set the log level back to `INFO` or `WARN` for normal operation to avoid excessive disk space usage and performance impact.
```

## 8. Basic Troubleshooting

This section provides guidance on diagnosing common issues.

### A. Server Fails to Start

If the Jagornet-DHCP server does not start, here are common areas to investigate:

1.  **Check Java Installation:**
    *   **Verify Version:** Ensure you have Java Development Kit (JDK) version 11 or newer. Run `java -version`.
    *   **Check PATH/JAVA_HOME:** Make sure the `java` executable is in your system's PATH, or that the `JAVA_HOME` environment variable is correctly set to your JDK installation directory. The server scripts often rely on these.

2.  **Review Logs (Crucial First Step):**
    *   **Application Logs:** Check `$JAGORNET_DHCP_HOME/logs/JagornetDhcpServer.log`. Look for Java stack traces or error messages that indicate why the application itself failed to initialize or run.
    *   **Service Logs:**
        *   On Linux with systemd: Run `sudo journalctl -u jagornet-dhcp -xe` to see startup failures.
        *   On Windows with WinSW: Check `%JAGORNET_DHCP_HOME%\logs\jagornet-service.log` or Windows Event Viewer.

3.  **Configuration File Errors:**
    *   **Validate Syntax:** Use the `--test-configfile` option to check your configuration file for syntax errors. Navigate to `$JAGORNET_DHCP_HOME` and run:
        ```bash
        # For Linux/macOS, assuming config is in config/dhcpserver.xml
        ./bin/dhcpserver --test-configfile -c config/dhcpserver.xml 
        ```
        ```cmd
        REM For Windows, assuming config is in config\dhcpserver.xml
        bin\dhcpserver.bat --test-configfile -c config\dhcpserver.xml
        ```
        Adjust the path if you are using a different configuration file name or location.
    *   **Correct File Loaded:** Ensure the server is attempting to load the configuration file you intend to use. If you start the server without the `-c` option, it looks for default names like `dhcpserver.xml` in the `config/` directory.

4.  **Port Conflicts:**
    *   **Required Ports:** DHCP servers need exclusive access to specific UDP ports:
        *   DHCPv4: Port 67 (server) and Port 68 (client messages often originate from here)
        *   DHCPv6: Port 547 (server) and Port 546 (client messages often originate from here)
    *   **Check Logs for Errors:** Look for messages like "Address already in use," "bind failed," or similar in `JagornetDhcpServer.log`.
    *   **Identify Conflicting Applications:** Ensure no other DHCP server (like another instance of Jagornet-DHCP, `dnsmasq`, ISC DHCPD, or a router's built-in DHCP server if on the same machine) or other application is already using these ports.
        *   **Linux/macOS:** Use `sudo netstat -tulnp | grep -E ':(67|68|546|547)'` or `sudo ss -tulnp | grep -E ':(67|68|546|547)'`.
        *   **Windows:** Use `netstat -ano -p udp | findstr ":67"` and similarly for other ports. Then use Task Manager (Details tab, find PID) or `tasklist /svc /FI "PID eq <PID>"` to find the process.
    *   **Privileged Ports (Linux/macOS):** Ports below 1024 are privileged. If you try to run `./bin/dhcpserver` directly without `sudo`, it will fail to bind to these ports. Using `sudo` or running as a service (which is usually configured to run as root or with necessary capabilities) is required.

5.  **Permissions (Linux/macOS):**
    *   **Execute Permissions:** Ensure the server scripts have execute permissions (e.g., `chmod +x ./bin/dhcpserver ./bin/testclientv4 ./bin/testclientv6 ./bin/gentestconfig`). This was covered in the "Important: Script Permissions" note earlier.
    *   **Privileged Ports:** As mentioned above, direct execution for privileged ports requires `sudo`. Service installations typically handle this.

### B. Clients Not Receiving IP Addresses

If clients are not getting IP addresses or DHCP configuration:

1.  **Verify Server Status:**
    *   Confirm that the Jagornet-DHCP server process is actually running. Use the status commands outlined in "Section 7B: Checking Server Status and Logs."

2.  **Check Server Logs:**
    *   Examine `$JAGORNET_DHCP_HOME/logs/JagornetDhcpServer.log` for any activity.
    *   Look for incoming DHCPDISCOVER (v4) or SOLICIT (v6) messages from the client's MAC address.
    *   Note any error messages related to processing these requests, selecting a subnet/link, or allocating an address from a pool.
    *   Increase log verbosity in `$JAGORNET_DHCP_HOME/config/log4j2.xml` (e.g., to DEBUG level for relevant loggers) if more detail is needed, then restart the server.

3.  **Client Network Configuration:**
    *   **Automatic IP:** Double-check that the client machine's network adapter is configured to "Obtain an IP address automatically" (for IPv4) and "Obtain IPv6 address automatically" (for IPv6).
    *   **Correct Network Segment:** Ensure the client is on the same physical or virtual network segment (LAN/VLAN) that the Jagornet-DHCP server is configured to listen on for that client's expected subnet/link.

4.  **Network Connectivity:**
    *   **Basic Checks:** Verify physical network connections (cables plugged in, Wi-Fi connected to the correct network).
    *   **Switches/Routers:** If there are managed switches or routers between the client and server, ensure they are not blocking DHCP traffic (e.g., DHCP snooping configured incorrectly, VLAN misconfiguration).
    *   **DHCP Relay:** If the client is on a different subnet from the server, ensure a DHCP relay agent (IP helper address on a router) is correctly configured to forward DHCP requests to the Jagornet-DHCP server's IP address.

5.  **Firewall Issues:**
    *   **Server-Side Firewall:** The firewall on the machine running Jagornet-DHCP must allow incoming UDP traffic on:
        *   Port 67 and 68 for DHCPv4.
        *   Port 547 and 546 for DHCPv6.
        Check `ufw` (Linux), `firewalld` (Linux), or Windows Firewall rules.
    *   **Client-Side Firewall:** While less common for DHCP, a very restrictive client-side firewall could potentially block outgoing DHCP requests or incoming offers.

6.  **Scope/Pool Configuration (Server-Side):**
    *   **Link Matching:** Verify that the client's network is correctly defined as a `<v4Link>` or `<v6Link>` in your `dhcpserver.xml`.
        *   For IPv4, the server matches based on the source IP address of the relay agent or by matching the server's own interface IP to a link's subnet.
        *   For IPv6, ensure the `<interface>` specified in the `<v6Link>` is correct, active, and the one the client's requests are arriving on. Use `./bin/dhcpserver list-interfaces` to verify interface names.
    *   **Address Pools:**
        *   Ensure `<v4AddrPools>` (for IPv4) or `<v6NaAddrPools>` (for stateful DHCPv6) are defined for the link.
        *   Check that these pools have available IP addresses. The log file might indicate if a pool is exhausted.
    *   **Lease File:** Check the lease file (e.g., `config/dhcpd.leases`) to see if leases are being recorded.

### C. Clients Receive Incorrect IP Information

If clients get an IP address but other details (like DNS servers, router, domain name) are wrong:

1.  **Review Server Configuration:**
    *   **Correct Link:** Carefully examine the `<v4ConfigOptions>` (for DHCPv4) or `<v6MsgConfigOptions>` (for DHCPv6) within the specific `<v4Link>` or `<v6Link>` that is serving the client.
    *   **Option Values:** Double-check the IP addresses for routers, DNS servers, NTP servers, etc., and any domain names or lease times defined. Typos are common.
    *   **Global vs. Link-Specific:** Remember that options can be defined globally and overridden at the link level. Ensure the configuration you are checking is the one being applied to the client.
2.  **Editing the Correct File:**
    *   Confirm you are editing the exact configuration file that the Jagornet-DHCP server is loading. If you made changes to a backup or a differently named file, the server won't see them unless you explicitly tell it to load that file (using the `-c` option at startup).
    *   Restart the server after any configuration changes to ensure they are applied.
3.  **Old Leases / Other DHCP Servers:**
    *   The client might be holding onto an old lease from a previous configuration or a different DHCP server on the network. Try releasing and renewing the lease on the client (see Section 7C).
    *   Ensure no other DHCP server on the network is unintentionally providing configuration to clients. This can lead to conflicting information.

If problems persist, consider increasing the logging level in `config/log4j2.xml` to `DEBUG` or even `TRACE` for relevant Jagornet-DHCP components to get more detailed insight into how the server is processing requests and options. Remember to set it back to `INFO` or `WARN` for normal operation to avoid excessive disk space usage.
```

## 9. Further Information

This quick start guide is intended to get you started with basic functionality. For more advanced configurations, detailed explanations of all features, and a deeper understanding of Jagornet-DHCP, please refer to the following resources typically found within the distributed server package in your `$JAGORNET_DHCP_HOME` directory:

*   **Sample Configuration Files (`config/samples/` directory):**
    *   The `config/samples/` directory contains several example configuration files that showcase more complex setups than this quick start guide.
    *   Files like `dhcpserver-sample.xml`, `dhcpserver-sample.json`, and `dhcpserver-sample.yaml` often demonstrate a wider range of options, including advanced address pool configurations, client classing, filter definitions, dynamic DNS updates, and various DHCPv4 and DHCPv6 options.
    *   The `dhcpserver-basic.xml` (and its JSON/YAML counterparts) provide a cleaner starting point than the more comprehensive samples but are still more detailed than the snippets in this guide.
    *   Review these samples to understand how to implement specific features.

*   **XML Schema Definition (`config/schema/dhcpserver.xsd`):**
    *   If you are using XML for your configuration (`dhcpserver.xml`), the XML Schema Definition (XSD) file located at `config/schema/dhcpserver.xsd` (the version number in the XSD filename might vary, e.g., `dhcpServerConfig-1.2.xsd`) is an invaluable resource.
    *   This schema formally defines the entire structure of the XML configuration file, including all available elements, attributes, their expected data types, and hierarchical relationships.
    *   Using an XML editor that supports schema validation can help you create valid configurations and explore available options directly.

*   **Documentation in `docs/` Directory:**
    *   The `$JAGORNET_DHCP_HOME/docs/` directory may contain additional documentation files.
    *   Look for files like `README.txt`, `release-notes.txt`, or other text files that might provide project information, specific version notes, advanced usage hints, or details not covered in this quick start.
    *   (Note: While a comprehensive user manual in PDF or HTML format might not always be present in every release, any available discrete documentation files will be located here.)

*   **Project Website / Repository:**
    *   For the most current information, including access to the source code, issue tracker, and potentially a Wiki or further documentation, always refer to the official project repository:
        [https://github.com/jagornet/Jagornet-DHCP](https://github.com/jagornet/Jagornet-DHCP)

By exploring these resources, you can unlock the full potential of the Jagornet-DHCP server and tailor it to your specific network requirements.
```
