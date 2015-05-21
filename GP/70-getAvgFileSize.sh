#!/bin/bash

# Create genData files, which just contain the info from each generation
../.././70-genSplitter.sh *nfo.log

# Finds the average file size
for genFile in `ls -tr genData* | grep -v Fit`
do 
  echo -en "\n"$genFile" "
  for file in `cat $genFile | grep + | awk '{ print $3".java" }'`; do cat $file | grep -B 100 -m 1 ^} | wc -l ; done | awk '{ sum+=$1} END { printf "%f",sum/NR}'

done

# Searches for any optimised lines
#for javaFile in `find $1/*.java`
#do
#    diff -q -B <(cat $javaFile  | grep -B 30 -m 1 ^} | egrep -v "Sort1Pr|Orig" ) <(cat /home/bck/GP/OptimisedBubble.java| egrep -v "Orig" )
#done
