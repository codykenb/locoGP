#!/bin/bash

# what file do we run this on? (*run.log)

genCount=0
fileName="genData"$genCount".txt"

while read line
  do
  mentionCount=`echo $line | grep -n Generation`
  if [ -z "$mentionCount" ]
    then
    echo $line >> $fileName
  else
    genCount=`echo $line | awk '{ print $6}'` #$(($genCount+1))
    fileName="genData"$genCount".txt"
    echo "" > $fileName
  fi
done < $1

