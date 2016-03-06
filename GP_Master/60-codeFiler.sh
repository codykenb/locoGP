#!/bin/bash


# For each txt file representing a single generation

# move all files belonging to that gen to their own dir


srcDir="src"
targetDir=$srcDir/`echo $1 | awk -F. '{ print $1 }'`
mkdir -p $targetDir
#outFlag=false
#fileName=""

while read line 
  do
  fileName=`echo $line | grep + |  awk '{ print $7 }' `

  if [ -z $fileName ]
    then
    fileName=`echo $line | grep Mutating |  awk '{ print $6 }' `
  fi

  if [ -n $fileName ]
    then
    mv -v $srcDir/$fileName.java $targetDir
  else
    echo -e "Filename not for for line: $line"
  fi
done < $1

