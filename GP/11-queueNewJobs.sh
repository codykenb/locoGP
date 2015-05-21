#!/bin/bash                                                                                                                      
set +e

# Author: Brendan Cody-Kenny 30-April-2014                                                                                       
# Queue new jobs, split jobs which have multiple comparisons

newJobsQueue=$1
runningJobsQueue=$2
graphableJobs=$3

while read aJob
do
    echo $aJob >> $graphableJobs
    origJobLine="$aJob"
    while [ -n "$aJob" ]
    do
        numRuns=`echo $aJob | awk '{ print $1 }'`
        jarFile=`echo $aJob | awk '{ print $2 }'`
        echo "$numRuns $jarFile" >> $runningJobsQueue
	tempJob=$(echo $aJob | awk '!($1="")')
	aJob=$(echo $tempJob | awk '!($1="")')
    done
    sed -i "/^$origJobLine/d" $newJobsQueue
    sed -i '/^$/d' $newJobsQueue
done < $newJobsQueue