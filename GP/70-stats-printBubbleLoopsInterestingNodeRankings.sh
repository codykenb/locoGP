#!/bin/bash

# Script for biasaccumulator results specifically for bubblesort alogirthm
# Find the average ranking of the nodes in bubblesort which need to change to optimise

## Should run against Seed files

problemName="sort1Loops1"
GPnum="GP117"

allNodeBiasFile="allNodesAllGensBias.txt"
importantVals=( 3 6 8 20 21 30 32 )

uniqueJobs=($(ls | grep $problemName | grep $GPnum | awk -F- '{ print $4 }' | sort | uniq))

echo "| | Rank | Abs | Node 3  | Rank | Abs | Node 6 | Rank | Abs | Node 8 | Rank | Abs | Node 20 | Rank | Abs | Node 21 | Rank | Abs | Node 30 | Rank | Abs | Node 32 |"



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
	cat $biasDir/Seed*bias | awk '{ print $1" "$3 }' | grep "\." >> $fullFilename
    done
    
    echo " " > $fullFilename.averageRank
    for i in  {1..72}
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




