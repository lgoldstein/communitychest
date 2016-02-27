#!/bin/bash

##############################################################################

updateClasspath () {
#	echo Scanning $1 - current libs=${APP_CLASSPATH}

	for jarfile in $1/*.jar; do
		case ${APP_CLASSPATH} in
		 	'') APP_CLASSPATH=${jarfile};;
			 *) APP_CLASSPATH=${APP_CLASSPATH}:${jarfile};;
		esac
	done

#	echo Scanned $1 - current libs=${APP_CLASSPATH}
}

##############################################################################

SCRIPT_PATH=`readlink -f $0`
APP_FOLDER=`dirname $SCRIPT_PATH`
# echo Executing in $APP_FOLDER
APP_BIN_FOLDER=${APP_FOLDER}/bin
APP_LIB_FOLDER=${APP_FOLDER}/lib
APP_CLASSPATH=

for jardir in ${APP_LIB_FOLDER} ${APP_BIN_FOLDER} ; do
	updateClasspath ${jardir}
done

# echo found LIB files: $APP_CLASSPATH

if [ "$1" == "-vm" ] ; then
	shift
	JAVA_HOME=$1
#	echo Using provided JVM=$JAVA_HOME
#else
#	echo Using default JVM=$JAVA_HOME
fi

case ${JAVA_HOME} in
	'') echo "JAVA_HOME not defined"
	    exit -1;;

	*) ${JAVA_HOME}/bin/java -cp $APP_CLASSPATH net.community.apps.tools.b64xlate.Main;;
esac
