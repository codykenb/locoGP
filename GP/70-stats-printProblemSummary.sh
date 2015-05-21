#!/bin/bash

exps=( 'ook' 'tail' 'hell' 'erge' 'sertion' 'election' 'election2' 'eap' 'ocktail' 'dix' 'ort' 'uick')


# for file in `ls GP*-sort-*/*/*nfo.log ` ; do grep =\ 1.0\  $file | awk '{ print $12}' ; done | sort | uniq -c | awk '{ t=$1; $1 = $2; $2=t; print; }' | sort -nr

# find out the last program number under noBias
for experiment in ${exps[@]}    #$(ls | grep GP117 | grep noBias | awk -F- '{ print $2 }' | sort | uniq)
do
    echo -ne "\n Experiment: $experiment "
    for file in $(ls GP117*$experiment-*noBias*/*/*nfo.log)
    do  
	tail -n500  $file | grep $experiment | awk '{ print $3 }' | awk -F$experiment '{ print $2 }' | sed 's/Problem//' | sed 's/1Test//' |  sort -n | tail -n1 | tr '\n' ' '
    done
done
