#!/bin/sh
# ------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ------------------------------------------------------------------------

# provide default values for people who don't use RPMs
if [ -z "$usejikes" ] ; then
  usejikes=false;
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
           fi
           ;;
esac

if [ -z "$DHCPV6_HOME" ] ; then
  # try to find DHCPV6
  if [ -d /opt/dhcpv6 ] ; then
    DHCPV6_HOME=/opt/dhcpv6
  fi

  if [ -d "${HOME}/opt/dhcpv6" ] ; then
    DHCPV6_HOME="${HOME}/opt/dhcpv6"
  fi

  ## resolve links - $0 may be a link to dhcpv6's home
  PRG="$0"
  progname=`basename "$0"`
  saveddir=`pwd`

  # need this for relative symlinks
  dirname_prg=`dirname "$PRG"`
  cd "$dirname_prg"

  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done

  DHCPV6_HOME=`dirname "$PRG"`/..

  cd "$saveddir"

  # make it fully qualified
  DHCPV6_HOME=`cd "$DHCPV6_HOME" && pwd`
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$DHCPV6_HOME" ] &&
    DHCPV6_HOME=`cygpath --unix "$DHCPV6_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$DHCPV6_BASE" ] ; then
  DHCPV6_BASE="$DHCPV6_HOME"
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  DHCPV6_HOME=`cygpath --windows "$DHCPV6_HOME"`
  DHCPV6_BASE=`cygpath --windows "$DHCPV6_BASE"`
  DHCPV6_CLASSPATH=`cygpath --path --windows "$DHCPV6_CLASSPATH"`
  JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  CYGHOME=`cygpath --windows "$HOME"`
fi

if [ -z "$DHCPV6_OPTS" ] ; then
  DHCPV6_OPTS="-Xmx512M"
fi

if [ -z "$SUNJMX" ] ; then
  #SUNJMX="-Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
  SUNJMX="-Dcom.sun.management.jmxremote"
fi

if [ -z "$SSL_OPTS" ] ; then
  SSL_OPTS="-Djavax.net.ssl.keyStorePassword=password -Djavax.net.ssl.trustStorePassword=password -Djavax.net.ssl.keyStore=${DHCPV6_BASE}/conf/broker.ks -Djavax.net.ssl.trustStore=${DHCPV6_BASE}/conf/broker.ts"
fi

DHCPV6_OPTS="$DHCPV6_OPTS $SUNJMX $SSL_OPTS"

# Set default classpath
DHCPV6_CLASSPATH="${DHCPV6_BASE}/conf;"$DHCPV6_CLASSPATH

# Uncomment to enable YourKit profiling
#DHCPV6_DEBUG_OPTS="-agentlib:yjpagent"

# Uncomment to enable remote debugging
#DHCPV6_DEBUG_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

if [ -n "$CYGHOME" ]; then
    exec "$JAVACMD" $DHCPV6_DEBUG_OPTS $DHCPV6_OPTS -Ddhcpv6.classpath="${DHCPV6_CLASSPATH}" -Ddhcpv6.home="${DHCPV6_HOME}" -Ddhcpv6.base="${DHCPV6_BASE}" -Dcygwin.user.home="$CYGHOME" -jar "${DHCPV6_HOME}/bin/run.jar" start $@
else
    exec "$JAVACMD" $DHCPV6_DEBUG_OPTS $DHCPV6_OPTS -Ddhcpv6.classpath="${DHCPV6_CLASSPATH}" -Ddhcpv6.home="${DHCPV6_HOME}" -Ddhcpv6.base="${DHCPV6_BASE}" -jar "${DHCPV6_HOME}/bin/run.jar" start $@
fi

