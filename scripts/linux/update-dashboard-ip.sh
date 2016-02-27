#!/bin/bash

#DASHBOARD_LOCATION=...wherever the default is...

if [ $# -ge 1 ];then
   DASHBOARD_LOCATION="$1"
fi

if [ $DASHBOARD_LOCATION == "" ];then
        echo "Dashboard location not provided - exiting"
        exit 1
fi

PROPS_LOCATION=$DASHBOARD_LOCATION/insight/insight.properties
if [ ! -e $PROPS_LOCATION ]; then
        exit "Properties file not found at $PROPS_LOCATION"
fi

ETH0IP=`/sbin/ifconfig  | awk '{ if ($0 ~/(.*Link encap:.*)/) { printf "%s", $1 } else { if ($0 ~/(.*inet addr:.*)/) printf " %s\n", $2 } }' | grep eth0 | cut -d: -f2`

echo "Setting IP address to $ETH0IP in $PROPS_LOCATION"
sed -i 's~^[ \t]*dashboard.jms.bind.uri: ssl://.*:21234~dashboard.jms.bind.uri: ssl://'$ETH0IP':21234~g' $PROPS_LOCATION
echo "Done."
