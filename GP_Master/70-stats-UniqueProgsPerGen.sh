#!/bin/bash

# Get a list of files for each generation (from *nfo.log)
genCount=0
fileExt="genFileHashes.txt"
fileName=$genCount$fileExt


# ------------------------------------------ Functions ------------

fFindNewPrograms(){
    currentGen=$1
    newFileCount=0
    for hashLine in `cat $fileName | sort | uniq`
      do
      grep $hashLine `ls *$fileExt | grep -v $fileName` > /dev/null
      if [ $? -ne 0 ]; then # new file found
	  newFileCount=$(($newFileCount+1))
      fi

    done
    echo -n "New "$newFileCount
}

# ------------------------------------------ Main prog ------------

echo "" > $fileName

while read line
  do
  mentionCount=`echo $line | grep -n Generation`
  sortLine=`echo $line | grep -n Sort1Problem`
  if [ -z "$mentionCount" ]
    then
      if [ -n "$sortLine" ]
	  then
	  grep -B 100 -m1 \^} `echo $line | awk '{ print $3 }'`.java | grep -v Sort | md5sum >>$fileName
	  fi
  else
    echo Gen $genCount Unique `cat $fileName | sort | uniq | wc -l` `fFindNewPrograms $genCount`
    genCount=$(($genCount+1))
    fileName=$genCount$fileExt
    echo "" > $fileName
  fi
done < *nfo.log

# Todo: show the number of brand new programs generated in each generation
