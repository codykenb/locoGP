#!/bin/bash

echo "Use with caution, this will delete all files matching $1"
echo "Continue? (YES,no)"
read answer

if [ "$answer" == "YES" ]
then
  . ./10-setServers.sh
  parallel ssh {} "rm -rf GP/GP$1*" ::: ${scssCloud[@]}
  parallel ssh {} 'killall java' ::: ${scssCloud[@]}
#  sed -i.bak '/$1/d' GP-Q*txt
  rm -rf archive/GP$1*
  exit 0
fi  

echo "Will not continue on: $answer"
exit 1  
