#!/bin/bash

# Script for biasaccumulator results specifically for huffman alogirthm
# Find the average ranking of the nodes which need to change to optimise

problemName="huff"
GPnum="GP117"

allNodeBiasFile="allNodesAllGensBias.txt"
importantVals=( 336 338 329 328 340 )

# 336 : 1.0 : j < length - 1    (17 in bubble)
# 338 : 1.0 : length - 1        (19)
# 329 : 1.0 : i                 (10)
# 328 : 1.0 : i++               (9)
# 340 : 1.0 : 1                 (21)

uniqueJobs=($(ls | grep 2014 | grep $problemName | grep $GPnum | awk -F- '{ print $4 }' | sort | uniq))

 # echo "| | Rank | Abs | 17 (j < length - 1) | Rank | Abs | 19 (length - 1) | Rank | Abs | 10 (i) | Rank | Abs | 9 (i++) | Rank | Abs | 21 (1) |"
echo "| | Rank | Abs | 336 (j < length - 1) | Rank | Abs | 338 (length - 1) | Rank | Abs | 329 (i) | Rank | Abs | 328 (i++) | Rank | Abs | 340 (1) |"


for jobName in ${uniqueJobs[@]}
do 
    #echo -n "| "
    fullJobName=$GPnum$problemName$jobName
    # Print the job name
    echo -n "| $fullJobName " #| tr -d '\n'  
    
    fullFilename=$fullJobName-$allNodeBiasFile
    echo " " > $fullFilename
    for biasDir in `ls | grep $GPnum | grep "\-$problemName" | grep "\-$jobName\-"`
    do
	cat $biasDir/$problemName*bias | awk '{ print $1" "$3 }' | grep "\." >> $fullFilename
    done
    
    echo " " > $fullFilename.averageRank
    for i in  {1..410} # 
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




