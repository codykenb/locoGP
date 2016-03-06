#!/bin/bash

# This script checks experiments for inconsistency and errors

genToCheck="GP117"

dirNames=`ls archive/ | grep $genToCheck | grep -vi VS`

###################### Check file name matches experiment. 
for dir in `ls archive/ | grep $genToCheck | grep -vi VS`
do
    expectedExperiment=`echo $dir | awk -F\- '{ print $2 }' | sed 's/[0-9]//g' | tr '[:upper:]' '[:lower:]'`
    file=`ls archive/$dir/*/*nfo.log | head -n1`
    expStringinFile=`grep -m1 \= $file | awk '{ print $3 }' | sed 's/[0-9]//g' | tr '[:upper:]' '[:lower:]'`
    found=`echo $expStringinFile | grep -c $expectedExperiment`
    if [ "$found" -eq 0 ]
    then
	echo "$expectedExperiment not found in $file ($expStringinFile found instead)"
    fi
    if [ "$expStringinFile" != "$expectedExperiment" ]
    then
	echo "$expectedExperiment not found in $file ($expStringinFile found instead)"
    fi
    echo " "
    echo " ===================================================== "
    echo " "
done
    

