#!/bin/bash

# Script for biasaccumulator results specifically for bubblesort alogirthm
# Find out the ranking of each iimportant node, for each ruleset

# expects a file with the ranking of all nodes after 250 seedbiasaccumulator runs, like this:
# for dir in `ls | grep GP111`; do cd $dir ; for i in {0..249} ; do echo $i; diff Seed-Sort1ProblemTest*-$i.bias Seed-Sort1ProblemTest*-$((i+1)).bias ; done | grep \> | awk '{ print $2 }' | sort -nr | uniq -c | sort -nr > biasUpdateFrequencyAtLocations.txt ; cd ~/GP-testGround ; done


# 19,21,17 are interesting, node 9 (i++) and 10 (i) is most interesting

importantVals=( 17 19 21 10 9 ) # these are the most interesting nodes in bubblesort for optimisation

for file in `ls *11*/biasUp*` 
do 
    echo -n "| "
    echo -n $file | awk -F\- '{ print $2$3$4" " }' | tr '\n' '|'
    for val in ${importantVals[@]}
    do
	grep -n "\ $val$" $file  | awk -F: '{ print $1 }' | tr '\n' ' '
	echo -n " | "
    done
    echo " "
done




