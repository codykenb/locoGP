#!/bin/bash
set +e 

# Author: Brendan Cody-Kenny 30-April-2014
unset servers
#echo "Passed: -$@-"
if [ -z $1 ]
then
  # Deploy scripts to all reachable machines
    . ./10-setServers.sh
else
    servers=( $@ )
fi
#echo "Deploying scripts to: ${servers[@]}"
parallel -j100 rsync -az -e \"ssh  -oBatchMode=yes -o ConnectTimeout=5\" 88-repeatRun.sh 88-zipper.sh {}:GP/ ::: ${servers[@]} 2> /dev/null
parallel -j100 ssh -oBatchMode=yes -o ConnectTimeout=5 {} "chmod +x GP/*.sh" ::: ${servers[@]} 2> /dev/null