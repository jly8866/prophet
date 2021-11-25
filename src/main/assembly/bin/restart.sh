#!/bin/bash

PROPHET_HOME="$( dirname "$( cd "$( dirname "$0"  )" && pwd ) " )"

${PROPHET_HOME}/bin/stop.sh

${PROPHET_HOME}/bin/startup.sh
