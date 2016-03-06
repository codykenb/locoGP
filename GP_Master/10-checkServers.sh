#!/bin/bash

source 50-setServers.sh

# Find underutilised servers
for aServer in "${servers[@]}"
  do
    echo "--------------------------------$aServer----------------------------------------"
    ssh $aServer "hostname -f"
    ssh $aServer "mpstat -P ALL"
    echo "--------------------------------$aServer----------------------------------------"
  done

