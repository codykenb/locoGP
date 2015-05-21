#!/bin/bash

source 50-setServers.sh

for aServer in "${servers[@]}"
  do
  #ssh $aServer "mp
  echo "deploying to server $aServer"
  ./10-deployServerDir.sh $aServer GP/GP57/rep10
  done

#./10-deployServerDir.sh frank GP/GP57
#./10-deployServerDir.sh frank GP/GP57
#./10-deployServerDir.sh frank GP/GP57
