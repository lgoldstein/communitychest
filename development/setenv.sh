# Edit this file to CATALINA_BASE/bin/setenv.sh to set custom options
# Tomcat accepts two parameters JAVA_OPTS and CATALINA_OPTS
# JAVA_OPTS are used during START/STOP/RUN
# CATALINA_OPTS are used during START/RUN

# JVM memory settings - general
GENERAL_JVM_OPTS="-Xmx512m "

# JVM Sun specific settings
# For a complete list http://blogs.sun.com/watt/resource/jvm-options-list.html
#SUN_JVM_OPTS="-XX:MaxPermSize=192m \
#              -XX:NewSize=128m \
#              -XX:MaxNewSize=256m \
#              -XX:MaxGCPauseMillis=500 \
#              -XX:HeapDumpOnOutOfMemoryError \
#              -XX:+PrintGCApplicationStoppedTime \
#              -XX:+PrintGCTimeStamps \
#              -XX:+PrintGCDetails \
#              -XX:+PrintHeapAtGC \
#              -Xloggc:gc.log"
SUN_JVM_OPTS="-XX:MaxPermSize=276m"
              
# JVM IBM specific settings
#IBM_JVM_OPTS=""

# Set any custom application options here
#APPLICATION_OPTS=""

# Must contain all JVM Options.  Used by AMS.
JVM_OPTS="$GENERAL_JVM_OPTS $SUN_JVM_OPTS"

CATALINA_OPTS="$JVM_OPTS $APPLICATION_OPTS"

#JAVA_HOME=setme
#JRE_HOME=setme

CBASE="$CATALINA_BASE"
if [ "$CBASE" = "" ] ; then
    CBASE="$CATALINA_HOME"
fi

CATALINA_OPTS="$CATALINA_OPTS -Dorg.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER=true -Dmail.mime.decodeparameters=true -Xms128m"
CATALINA_OPTS="$CATALINA_OPTS -Dinsight.base=$CBASE/insight -Dinsight.logs=$CBASE/logs -Djava.library.path=$LD_LIBRARY_PATH:$CBASE/insight/sigar-lib -Dgemfire.disableShutdownHook=true -Djava.awt.headless=true"
CLASSPATH="$CLASSPATH:$CBASE/bin/insight-bootstrap-tcserver-@TCSERVERBOOTSTRAP@.jar:$CBASE/lib/aspectjweaver-@ASPECTJWEAVER@.jar"
export CLASSPATH
