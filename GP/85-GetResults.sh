#!/bin/bash

source 50-setServers.sh

gpDir=$1

for aServer in "${servers[@]}"
do 
    echo "Retrieving data from $aServer:GP/$gpDir to $gpDir-$aServer"
    mkdir $gpDir-$aServer
    cd $gpDir-$aServer
#screen -d -m rsync -rav --exclude '*.class' $aServer:GP/$gpDir* .
    rsync -rav --exclude '*.class' $aServer:GP/$gpDir* . 
    cd ..
    rmdir $gpDir-$aServer
done
