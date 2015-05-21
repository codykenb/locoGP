#!/bin/bash

rootDir="/home/bck/GP/archive/"
experiments="GP117"
outDir="optimisedFiles"
mkdir -p $outDir


for dir in $(ls $rootDir | grep $experiments | grep -vi vs)
do
#  echo "getting files for $dir"
  bestLowestVal=1
  for expFile in $(ls $rootDir$dir/*/*nfo.log)
  do
      lowestVal=$(awk 'BEGIN{min=-1}{ if (($10 < min || min < 0)&& $10>0 ){ min = $10 } } END {print min}' $expFile)
      compareResult=`echo "$bestLowestVal >$lowestVal" | bc`
      if [ $compareResult -gt 0 ]
      then
	  fileName=$(grep -m1 "$lowestVal" $expFile | awk '{ print $3}')
	  javaArchDir=$(echo $expFile | awk -F/ 'BEGIN { OFS = "/"}{$NF=""; print $0}')
      fi
  done
  echo $expFile" "$fileName".java"
  grep -m1 $fileName $expFile
#tar -xzvf $javaArchDir"javaFiles.tgz" $fileName".java" -C $outDir/
done
