#!/bin/sh
#
# Jagornet DHCP Server - Linux systemd Service Installer
#

set -e

show_help() {
  cat << 'EOF'
Usage: install-service.sh [options]

Installs and configures the Jagornet DHCP Server as a systemd service.

Options:
  -n, --no-start          Install and enable the service, but do not start it immediately
  -d, --dir <path>        Target installation symlink path (default: /opt/jagornet/dhcp)
  -u, --user <username>   System user to run the service as (default: jagornet)
  -g, --group <groupname> System group to run the service as (default: jagornet)
  -h, --help              Show this help message and exit
EOF
}

START_SERVICE=true
TARGET_SYMLINK="/opt/jagornet/dhcp"
SERVICE_USER="jagornet"
SERVICE_GROUP="jagornet"

while [ $# -gt 0 ]; do
  case "$1" in
    -n|--no-start)
      START_SERVICE=false
      shift
      ;;
    -d|--dir)
      TARGET_SYMLINK="$2"
      shift 2
      ;;
    -u|--user)
      SERVICE_USER="$2"
      shift 2
      ;;
    -g|--group)
      SERVICE_GROUP="$2"
      shift 2
      ;;
    -h|--help)
      show_help
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      show_help >&2
      exit 1
      ;;
  esac
done

# 1. Privilege check
if [ "$(id -u)" -ne 0 ]; then
  echo "Error: This script must be run as root (e.g. sudo ./bin/install-service.sh)." >&2
  exit 1
fi

# 2. systemd check
if ! command -v systemctl >/dev/null 2>&1 || [ ! -d /run/systemd/system ]; then
  echo "Error: systemd was not detected or is not the active init system on this host." >&2
  exit 1
fi

# 3. Resolve installation directory
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
SCRIPT_DIR=`dirname "$PRG"`
SCRIPT_DIR=`cd "$SCRIPT_DIR" && pwd`
INSTALL_DIR=`cd "$SCRIPT_DIR/.." && pwd`

echo "=== Jagornet DHCP Server Service Installation ==="
echo "Source directory: $INSTALL_DIR"
echo "Target path:      $TARGET_SYMLINK"

# 4. Check Java availability
JAVA_CMD=""
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
  JAVA_CMD="java"
fi

if [ -n "$JAVA_CMD" ]; then
  JAVA_VER=$("$JAVA_CMD" -version 2>&1 | awk -F '"' '/version/ {print $2}' | head -n 1)
  echo "Java detected:    $JAVA_CMD (version $JAVA_VER)"
else
  echo "Warning: Java not found in PATH or JAVA_HOME." >&2
  echo "         Ensure Java 11+ is installed before starting the service." >&2
fi

# 5. Create symlink if needed
if [ "$INSTALL_DIR" != "$TARGET_SYMLINK" ]; then
  PARENT_DIR=`dirname "$TARGET_SYMLINK"`
  mkdir -p "$PARENT_DIR"

  if [ -e "$TARGET_SYMLINK" ] && [ ! -L "$TARGET_SYMLINK" ]; then
    echo "Warning: $TARGET_SYMLINK exists and is a regular directory/file."
  else
    ln -sfn "$INSTALL_DIR" "$TARGET_SYMLINK"
    echo "Created symlink:  $TARGET_SYMLINK -> $INSTALL_DIR"
  fi
else
  echo "Installation directory already matches $TARGET_SYMLINK"
fi

# 6. Create system group if not present
if getent group "$SERVICE_GROUP" >/dev/null 2>&1; then
  echo "Group:            '$SERVICE_GROUP' already exists"
else
  groupadd -r "$SERVICE_GROUP"
  echo "Created group:    '$SERVICE_GROUP'"
fi

# 7. Create system user if not present
if id -u "$SERVICE_USER" >/dev/null 2>&1; then
  echo "User:             '$SERVICE_USER' already exists"
else
  NOLOGIN="/usr/sbin/nologin"
  if [ ! -x "$NOLOGIN" ]; then
    if [ -x "/sbin/nologin" ]; then
      NOLOGIN="/sbin/nologin"
    else
      NOLOGIN="/bin/false"
    fi
  fi
  useradd -r -g "$SERVICE_GROUP" -d "$TARGET_SYMLINK" -s "$NOLOGIN" \
    -c "Jagornet DHCP Server" "$SERVICE_USER"
  echo "Created user:     '$SERVICE_USER' (system user, nologin)"
fi

# 8. Ensure runtime directories exist, scripts are executable, and set directory permissions
mkdir -p "$INSTALL_DIR/log" "$INSTALL_DIR/db"
chmod +x "$INSTALL_DIR"/bin/*
chown -R "$SERVICE_USER:$SERVICE_GROUP" "$INSTALL_DIR"
if [ -L "$TARGET_SYMLINK" ]; then
  chown -h "$SERVICE_USER:$SERVICE_GROUP" "$TARGET_SYMLINK"
fi
echo "Permissions:      Set ownership to $SERVICE_USER:$SERVICE_GROUP"

# 9. Create /etc/default/jagornet-dhcp template if not exists
if [ ! -f /etc/default/jagornet-dhcp ]; then
  mkdir -p /etc/default
  cat << 'EOF' > /etc/default/jagornet-dhcp
# Jagornet DHCP Server service environment overrides
# Uncomment and customize as needed:

# JAGORNET_DHCP_HOME=/opt/jagornet/dhcp
# JAVA_HOME=/usr/lib/jvm/default-java
# JAVA_OPTS="-Xms512m -Xmx2g"
EOF
  chmod 644 /etc/default/jagornet-dhcp
  echo "Environment file: Created /etc/default/jagornet-dhcp"
fi

# 10. Install systemd service unit
SERVICE_SRC="$INSTALL_DIR/bin/jagornet-dhcp.service"
SERVICE_DST="/etc/systemd/system/jagornet-dhcp.service"

if [ ! -f "$SERVICE_SRC" ]; then
  echo "Error: Service unit file not found at $SERVICE_SRC" >&2
  exit 1
fi

cp "$SERVICE_SRC" "$SERVICE_DST"
chmod 644 "$SERVICE_DST"
echo "Systemd unit:     Installed to $SERVICE_DST"

systemctl daemon-reload
systemctl enable jagornet-dhcp.service
echo "Service status:   Enabled on boot"

# 11. Start service if requested
if [ "$START_SERVICE" = "true" ]; then
  echo "Starting service: systemctl start jagornet-dhcp"
  systemctl start jagornet-dhcp.service
  echo ""
  echo "======================================================================"
  echo "Jagornet DHCP Server service installed and started successfully!"
  echo ""
  echo "  View status:  sudo systemctl status jagornet-dhcp"
  echo "  Follow logs:  sudo journalctl -u jagornet-dhcp -f"
  echo "  Stop service: sudo systemctl stop jagornet-dhcp"
  echo "======================================================================"
else
  echo ""
  echo "======================================================================"
  echo "Jagornet DHCP Server service installed and enabled (not started)."
  echo ""
  echo "  Start service: sudo systemctl start jagornet-dhcp"
  echo "  View status:   sudo systemctl status jagornet-dhcp"
  echo "  Follow logs:   sudo journalctl -u jagornet-dhcp -f"
  echo "======================================================================"
fi
