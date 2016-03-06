#!/bin/bash

# this file takes in -GenerationInfo.log file (V2)
# extracts only the fitnesses that exist in that particular generation
# Does NOT need a -GPrun.log file

# Nov 2014. New version of this file to prevent extra blank lines in the file #hackyhack

genCount=0
fileName="genDataFit"$genCount".txt"
echo "" > $fileName

echo "Creating genDataFit txt files for `pwd`"
while read line
  do
  mentionCount=`echo $line | grep -n Generation`
  if [ -z "$mentionCount" ]
    then
     fitness=`echo $line | grep \= | awk '{ print $10}' `
    echo $fitness >> $fileName
  else
    sed -ie '/^ *$/d' $fileName
    genCount=`echo $line | awk '{ print $6}'` #$(($genCount+1))
    fileName="genDataFit"$genCount".txt"
#    echo "Creating `pwd`/$fileName"
    echo "" > $fileName
  fi
done < $1


