#!/bin/bash

# Script for biasaccumulator results specifically for bubblesort alogirthm
# Find the average ranking of the nodes in bubblesort which need to change to optimise

problemName="sort"
GPnum="GP114"

allNodeBiasFile="allNodesAllGensBias.txt"
importantVals=( 17 19 10 9 21 )

uniqueJobs=($(ls | grep 2014 | grep $problemName | grep $GPnum | awk -F- '{ print $4 }' | sort | uniq))

echo "| | Rank | Abs | 17 (j < length - 1) | Rank | Abs | 19 (length - 1) | Rank | Abs | 10 (i) | Rank | Abs | 9 (i++) | Rank | Abs | 21 (1) |"

for jobName in ${uniqueJobs[@]}
do 
    #echo -n "| "
    fullJobName=$GPnum$problemName$jobName
    # Print the job name
    echo -n "| $fullJobName " #| tr -d '\n'  
    
    fullFilename=$fullJobName-$allNodeBiasFile
    echo " " > $fullFilename
    for biasDir in `ls | grep $GPnum | grep "\-$problemName\-" | grep "\-$jobName\-"`
    do
	cat $biasDir/Sort1ProblemTest*bias | awk '{ print $1" "$3 }' | grep "\." >> $fullFilename
    done
    
    echo " " > $fullFilename.averageRank
    for i in  {1..61}
    do 
	echo -n " $i "
	cat $fullFilename | sort -n | grep "^$i\ "| awk '{sum+=$2} END { print sum/NR }' 
    done | awk '{ print $2" "$1 }' | sort -nr | awk '{ count+=1 ; print count" | "$1" | "$2 }'  >> $fullFilename.averageRank

    for i in  ${importantVals[@]}
    do 
	echo -n " | "
	cat $fullFilename.averageRank | grep "\ $i$" | tr -d '\n'
    done

    echo " | "
done




