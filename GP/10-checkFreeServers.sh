#!/bin/bash

. ./10-setServers.sh

#parallel ssh -o ConnectTimeout=5 {} "hostname \&\& who"  ::: ${servers[@]} > rawServerList.txt 2> /dev/null

unset freeServers
#export freeServers=$(diff <(cat rawServerList.txt | grep -v -B1 lg12 | grep -v \\-\\-) <(cat rawServerList.txt) | grep \> | awk '{ print $2 }')
#echo ${lgServers[@]} > lastFreeLGservers.txt
#echo ${lgServers[@]}

# servers which have noone logged in, no loco jobs running, and have load < 0 are free
# Print hostname, if noone logged in or no locojobs, then no lines are printed. 
# Any lines starting with a "0", and the lines before them are kept. If the line before them is a server name, it will not be removed. 
tempServers=$(parallel ssh -oBatchMode=yes -o ConnectTimeout=5 {} "hostname \; who \;ps faux \\| grep loco \| grep -v grep \; cat /proc/loadavg \| awk \'{ print \\\$1 }\' "  ::: ${servers[@]} 2> /dev/null | grep -v loco |  grep -B1 "^0" | grep -v tty | grep -v \, | grep -v pts | grep -v ^0 | grep -v ^1 | grep -v \\-\\- | grep -v \:0 | grep -v \:1)
echo ${tempServers[@]}
export freeServers=( $tempServers )
