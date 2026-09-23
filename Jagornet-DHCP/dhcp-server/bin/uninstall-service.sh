#!/bin/sh
#
# Jagornet DHCP Server - Linux systemd Service Uninstaller
#

set -e

show_help() {
  cat << 'EOF'
Usage: uninstall-service.sh [options]

Stops, disables, and removes the Jagornet DHCP Server systemd service.

Options:
  -r, --remove-symlink    Remove the /opt/jagornet/dhcp symlink if pointing to this directory
  -h, --help              Show this help message and exit
EOF
}

REMOVE_SYMLINK=false

while [ $# -gt 0 ]; do
  case "$1" in
    -r|--remove-symlink)
      REMOVE_SYMLINK=true
      shift
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
  echo "Error: This script must be run as root (e.g. sudo ./bin/uninstall-service.sh)." >&2
  exit 1
fi

# 2. Resolve installation directory
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

echo "=== Jagornet DHCP Server Service Uninstallation ==="

# 3. Stop service if active
if command -v systemctl >/dev/null 2>&1; then
  if systemctl is-active --quiet jagornet-dhcp 2>/dev/null; then
    echo "Stopping service: systemctl stop jagornet-dhcp"
    systemctl stop jagornet-dhcp.service || true
  fi

  # 4. Disable service if enabled
  if systemctl is-enabled --quiet jagornet-dhcp 2>/dev/null; then
    echo "Disabling service: systemctl disable jagornet-dhcp"
    systemctl disable jagornet-dhcp.service || true
  fi
fi

# 5. Remove systemd unit file
SERVICE_DST="/etc/systemd/system/jagornet-dhcp.service"
if [ -f "$SERVICE_DST" ]; then
  rm -f "$SERVICE_DST"
  echo "Removed unit file: $SERVICE_DST"
fi

if command -v systemctl >/dev/null 2>&1; then
  systemctl daemon-reload || true
  systemctl reset-failed 2>/dev/null || true
fi

# 6. Remove symlink if requested and pointing to this installation
TARGET_SYMLINK="/opt/jagornet/dhcp"
if [ -L "$TARGET_SYMLINK" ]; then
  RESOLVED_SYMLINK=`cd "$TARGET_SYMLINK" 2>/dev/null && pwd || true`
  if [ "$RESOLVED_SYMLINK" = "$INSTALL_DIR" ]; then
    if [ "$REMOVE_SYMLINK" = "true" ]; then
      rm -f "$TARGET_SYMLINK"
      echo "Removed symlink:   $TARGET_SYMLINK"
    else
      echo "Note: Symlink $TARGET_SYMLINK was preserved."
      echo "      Pass --remove-symlink to remove it."
    fi
  fi
fi

echo ""
echo "======================================================================"
echo "Jagornet DHCP Server service uninstalled successfully."
echo ""
echo "Note: Application files, configuration, databases, logs, and the"
echo "'jagornet' system user have been preserved for safety."
echo "======================================================================"
