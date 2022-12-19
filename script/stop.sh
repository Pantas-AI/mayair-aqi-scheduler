#!/bin/bash

pid=`ps -ef | grep java | grep mayair-aqi.jar | awk '{printf $2}'`

if [ $pid ];
then
  kill -9 $pid
else
  echo "app not started"
fi