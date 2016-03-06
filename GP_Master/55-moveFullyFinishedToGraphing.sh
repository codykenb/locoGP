#!/bin/bash

# Author: Brendan Cody-Kenny 30-April-2014                                                                                         
# Review running jobs, retrieve finished jobs                                                                                        

#  ./50-retrieveFinished.sh GP-Queue-Pending.txt GP-Queue-Finished.txt                                                                
runningJobsQueue=$1
finishedJobsQueue=$2

# echo `date`" Checking for completed Jobs (local)"
while read -u 3 aJob
do
#    echo `date` "Checking: $aJob"
    if [ -n "$aJob" ]
    then
	
	numRunsRequired=`echo $aJob | awk '{ print $1 }'`
        jarFile=`echo $aJob | awk '{ print $2 }'`
        jobName=`echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g'`

#	jobNameTokens=`echo $jobName | tr '-' ' '`
#	numGPRunsRequired=$(for line in ${tokens[@]} ; do if [ `echo $line | grep Gen ` ] ; then echo $line ; fi ; done | tr 'Gen' ' ')
#	finishedSearchText="eneration: $((numGPRunsRequired-1))"
	jobletsFinished=`find archive/$jobName/rep*/*nfo.log 2>/dev/null | wc -l `

	echo "`date` $jobName joblets finished: $jobletsFinished / $numRunsRequired"
	if [ "$jobletsFinished" -ge "$numRunsRequired" ]
	then
	    echo "Job $jobName done! Cleaning up and passing to graphing..."
	    
	    . ./10-setServers.sh # exports $servers
	    parallel -j100 ssh -oBatchMode=yes -o ConnectTimeout=5 {} "rm -rf GP/$jobName 2> /dev/null " ::: ${servers[@]}
	    
	    echo $aJob >> $finishedJobsQueue

	    # take job out of running queue
	    sed -i "/^$aJob/d" $runningJobsQueue
	    sed -i '/^$/d' $runningJobsQueue
	    parallel -j100 ssh -oBatchMode=yes -o ConnectTimeout=5 {} "rm -rf GP/$jobName 2> /dev/null " ::: ${servers[@]}
	fi
    fi
#    echo `date` "Done checking: $aJob"
done 3< $runningJobsQueue
