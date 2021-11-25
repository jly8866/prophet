#!/bin/bash

PROPHET_HOME="$( dirname "$( cd "$( dirname "$0"  )" && pwd ) " )"

export JAVA_HOME=/apps/srv/jdk/bin

JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -Xmx1500m"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:+UseG1GC -verbose:gc"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:+PrintGCDetails"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:+PrintGCTimeStamps"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:+PrintGCDateStamps"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -Xloggc:${PROPHET_HOME}/logs/prophet-gc.log"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:HeapDumpPath=${PROPHET_HOME}/logs/heapdump.hprof"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:+UseGCLogFileRotation"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -XX:GCLogFileSize=128M -XX:NumberOfGCLogFiles=4"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -Dcom.sun.management.jmxremote=true"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -Dcom.sun.management.jmxremote.port=30005"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -Dcom.sun.management.jmxremote.ssl=false"
JAVA_CMD_OPTS="${JAVA_CMD_OPTS} -Dcom.sun.management.jmxremote.authenticate=false"

MAIN_CLASS="com.prophet.Application"

pid=`ps -ef |grep "java"|grep "prophet" |grep -v "grep" |awk '{print $2}'`
if [ -f ${PROPHET_HOME}/logs/prophet.pid ];then
    echo "Error! Prophet is running and pid is ${pid}, please stop it first."
    exit 1
else
    #set classpath
    for j in ${PROPHET_HOME}/lib/*.jar;do
        CLASSPATH=${j}:"${CLASSPATH}"
    done
    CLASSPATH="${PROPHET_HOME}/conf:${CLASSPATH}"

    #nohup java -jar
    nohup ${JAVA_HOME}/java ${JAVA_CMD_OPTS} -classpath .:${CLASSPATH} ${MAIN_CLASS} -Dglobal.config.path=${PROPHET_HOME}/conf/ --spring.config.location=${PROPHET_HOME}/conf/application.properties &>>${PROPHET_HOME}/logs/prophet.log &

    sleep 2
    pid=`ps -ef |grep "java"|grep "prophet" |grep -v "grep" |awk '{print $2}'`
    if [ ${pid} ];then
        echo "Prophet started successfully."
        echo ${pid} > ${PROPHET_HOME}/logs/prophet.pid
    else
        echo "Error! Prophet failed to start... please check the logs."
        exit 1
    fi
fi
exit 0
