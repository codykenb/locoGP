#!/bin/bash

# Script for biasAccumulator results specifically for huffbook alogirthm
# Find out how many unique nodes occupy the highest bias position
# Find out how many times the top node changes

hNodeFile="highestNodes.txt"

problemText="huff"

echo "------------------------------------------------------Running for $problemText"

echo "| Rule | # unique top nodes | # top node changes | # interesting nodes are top | # unique interesting as top | # most insteresting are top| last ind # |"

for file in `ls | grep 2014 | grep -i $problemText | grep R[34]` 
do 
    cd $file
    echo -n "| "

    # Print the job name
    echo -n $file | awk -F\- '{ print $2$3$4" " }' | tr '\n' '|'

    # get 
    if [ ! -f $hNodeFile ]
    then 
  		for i in {0..249}
    		do
			cat  Seed-$problemText*-$i.bias | awk '{ print $3" "$1}' | sort -n | tail -n1 2> /dev/null
    		done > $hNodeFile
    fi

    # print the # of unique nodes which have highest bias
    cat $hNodeFile | awk '{ print $2}' | sort -nr | uniq -c | wc -l | tr '\n' ' '
    echo -n " | "

    # number of times the highest bias node changes order
    cat $hNodeFile  | awk '{ print $2}' | uniq -c | wc -l | tr '\n' ' '
    echo -n " | "

# 336 : 1.0 : j < length - 1    (17 in bubble)
# 338 : 1.0 : length - 1        (19)
# 329 : 1.0 : i                 (10)
# 328 : 1.0 : i++               (9)
# 340 : 1.0 : 1                 (21)

    # number of times an interesting node has the highest bias
    # 17 19 21 10 9 
    cat $hNodeFile | grep -E '\ 336$|\ 328$|\ 329$|\ 338$|\ 340$' | wc -l | tr '\n' ' '
    echo -n " | "

    # number of unique interesting nodes which reach the top position
    cat $hNodeFile | grep -E '\ 336$|\ 328$|\ 329$|\ 338$|\ 340$' | awk '{ print $2}' | sort -n | uniq| wc -l | tr '\n' ' '
    echo -n " | "

    # number of times the most interesting nodes are top
    cat $hNodeFile | grep -E '\ 328$|\ 340$' | wc -l | tr '\n' ' '
    echo -n " | "

    # print number of the last program (gives a guage of number of retries performed)
    # Ideally this should be as low as possible, while visiting interesting nodes as much as possible
    ls huff*bias | awk -Fn '{ print $3 }' | sort -n | awk -F- '{ print $1}' | tail -n 1 | tr '\n' ' '
    echo -n " | "

    echo " "
    cd - > /dev/null
done




