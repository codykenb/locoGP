#!/bin/bash

# this file takes in -GenerationInfo.log file
# extracts only the fitnesses that exist in that particular generation
# needs a -GPrun.log file also

genCount=0
fileName="genDataFit"$genCount".txt"

while read line
  do
  mentionCount=`echo $line | grep -n Generation`
  if [ -z "$mentionCount" ]
    then
    
    indName=`echo $line | awk '{ print $3 }'`
    #echo $indName
    fitnessLine=`cat $2 | grep "$indName\ T" | sort -rn | head -n 1 `
    #echo $fitnessLine
    
    xoverResult=`echo $fitnessLine | grep -c +`    
    
    if [ $xoverResult -eq 1 ]
    then
        fitness=`echo $fitnessLine | awk '{ print $10 }' | awk -F\: '{ print $2 }'`
    else
        fitness=`echo $fitnessLine | awk '{ print $9 }' | awk -F\: '{ print $2 }'`
    fi
    #echo $fitness
    echo $fitness >> $fileName
  else
    genCount=`echo $line | awk '{ print $4}'` #$(($genCount+1))
    fileName="genDataFit"$genCount".txt"
    echo "Creating `pwd`/$fileName"
    
    echo "" > $fileName
  fi
done < $1


