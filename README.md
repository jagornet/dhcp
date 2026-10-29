# Jagornet DHCP Server

[![Release](https://img.shields.io/github/v/release/jagornet/dhcp?color=blue)](https://github.com/jagornet/dhcp/releases)
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org/)
[![IPv6 Ready](https://img.shields.io/badge/IPv6_Ready-Phase_II_Certified-blue.svg)](https://www.ipv6ready.org/)

**Jagornet DHCP Server** is an open source, enterprise-grade DHCPv4 and DHCPv6 server built on a high-performance, multithreaded [Netty](https://netty.io) network engine. 

Designed for cloud, container, and on-premises environments, Jagornet delivers standards compliance, high availability, and modern observability within an all-in-one, lightweight runtime.

---

## Why Jagornet?

* **Unified Single-Service Model:** Run DHCPv4, DHCPv6, Dynamic DNS (DDNS), High Availability (HA), and REST management within a single service. Avoid the operational overhead and daemon sprawl of multi-process architectures that require orchestrating 4–5 separate processes.
* **Modern Replacement for Legacy Servers:** An active, standards-compliant drop-in successor for organizations migrating away from end-of-life (EOL) ISC DHCP or seeking an operationally simpler alternative to Kea.
* **Built-in High Availability:** High-speed active/standby failover powered by [gRPC](https://grpc.io) state synchronization, with no external clustering dependencies required.
* **Cloud-Native & Container-Ready:** Ships with Docker/OCI support, native Prometheus metrics, OpenAPI REST APIs, and subnet/server-ID overrides tailored for cloud topologies.

---

## Key Capabilities

### 🌐 Dual-Stack & Protocol Standards
* **DHCPv4:** Fully compliant (RFC 2131, RFC 2132) including BOOTP support.
* **DHCPv6:** IPv6 Ready Phase II Certified for stateful/stateless addressing (RFC 3315, RFC 3736), prefix delegation (RFC 3633), and DNS options (RFC 3646).
* **Flexible Policy & Scopes:** Static reservations, client class filtering, and MAC/DUID inclusion/exclusion pools.
* **Dynamic DNS (DDNS):** Multi-strategy forward and reverse DNS updates with built-in hostname generation and validation (RFC 4703, RFC 4704).

### 🛡️ Enterprise Resilience & Storage
* **High Availability:** Primary/backup clustering via [gRPC](https://grpc.io) with automated lease synchronization.
* **Pluggable Storage:** Flexible lease persistence backends including **PostgreSQL**, **MySQL / MariaDB**, **SQLite**, and embedded **H2**.

### 📊 Modern Management & Observability
* **Prometheus Metrics:** Out-of-the-box telemetry for lease allocations, request rates, error counters, and server health.
* **REST API:** Complete Swagger / OpenAPI 3.0 interface for programmatic configuration and integration.
* **Flexible Configuration:** Author and manage configurations in **YAML**, **JSON**, or **XML** (validated against our published schema).

### ⚡ Scalability & Portability
* High-throughput, non-blocking I/O powered by [Netty](https://netty.io).
* Cross-platform support across Linux, macOS, and Windows running on Java 21+.
* Pre-configured systemd service scripts (Linux) and Windows Service Wrapper (WinSW).

---

## 🚀 Quick Start

### Running with Docker

Run a standalone Jagornet DHCP server instance:

```bash
docker run -d \
  --name jagornet-dhcp \
  --net=host \
  -v $(pwd)/config:/app/config \
  jagornet/dhcp:5.0.0
```

### Running from Distribution

1. Download and extract the latest release archive (`.tar.gz` or `.zip`) from [GitHub Releases](https://github.com/jagornet/dhcp/releases).
2. Ensure `JAVA_HOME` points to Java 21+.
3. Start the server:

```bash
# Linux / macOS (requires root or sudo for default ports 67/547)
sudo ./bin/dhcpserver -c config/dhcpserver.yaml

# Windows (Command Prompt as Administrator)
bin\dhcpserver.bat -c config\dhcpserver.yaml
```

*(For production systemd service installation or Windows Service setup, refer to the [User Guide](https://www.jagornet.com/documentation).)*

### ⚡ 60-Second Evaluation (Local Test Without Root)

Quickly test lease allocation on your workstation without `sudo` or impacting your local network:

1. **Generate a test DHCPv4 configuration** matching your active network interface:
   ```bash
   ./bin/gentestconfig -v 4 -i eth0   # or ens1, en0, etc.
   ```
2. **Start the server on an unprivileged test port**:
   ```bash
   ./bin/dhcpserver -4p 10067 -c dhcpserver-test-config.xml
   ```
3. **Simulate DHCPv4 client lease requests** in a separate terminal:
   ```bash
   ./bin/testclientv4 -sp 10067
   ```

---

## Documentation & Resources

* 📖 **Documentation:** [Jagornet DHCP Server User Guide](https://www.jagornet.com/documentation)
* 📋 **Config Schema:** [dhcpserver.xsd Schema](https://raw.githubusercontent.com/jagornet/dhcp-docs/main/dhcpserver.xsd)
* 🔌 **REST API Specification:** [OpenAPI 3.0 Specification](https://raw.githubusercontent.com/jagornet/dhcp-docs/main/openapi.yaml)
* 📦 **Downloads & Releases:** [GitHub Releases](https://github.com/jagornet/dhcp/releases)
* 💬 **Community & Support:** [GitHub Discussions](https://github.com/jagornet/dhcp/discussions)
* 🌐 **Website:** [Jagornet Technologies](https://www.jagornet.com)

---

## License

Jagornet DHCP Server is licensed under the [Mozilla Public License 2.0 (MPL 2.0)](LICENSE.md).

