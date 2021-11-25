#!/bin/bash

PROPHET_HOME="$( dirname "$( cd "$( dirname "$0"  )" && pwd ) " )"

pid=`ps -ef |grep "java"|grep "prophet" |grep -v "grep" |awk '{print $2}'`
if [ ${pid} ];then
    kill -9 ${pid}
    sleep 1
    if [[ $? -eq 0 ]];then
        echo "Prophet stopped successfully."
        rm -f ${PROPHET_HOME}/logs/prophet.pid &>/dev/null
    else
        echo "Error! Prophet failed to stop..."
    fi
else
    echo "Prophet is not running, no need to stop."
fi
exit 0
