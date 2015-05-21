#!/bin/bash                                                                                                                             
# set -e # the "parallel" call throws an error if machines are offline

# Author: Brendan Cody-Kenny 30-April-2014
# Control running jobs

runningJobsQueue=$1

if [ -z $runningJobsQueue ]
then
    echo "Jobs Queue Not Found, Exiting"
    exit 1
fi

. ./10-setServers.sh # exports $servers
#echo `date` Beginning Jobs Queue
while read -u 3 aJob
do
#    echo `date` "Checking: $aJob"
    if [ -n "$aJob" ]
    then
	numRunsRequired=`echo $aJob | awk '{ print $1 }'`
	jarFile=`echo $aJob | awk '{ print $2 }'`
	jobName=`echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g'`

	# How many joblets are deployed?
	# TODO If a job is deployed, but neither running or finished, it should be deleted
	# TODO when rerunning a larger job (e.g. a job for 200 after 100 has already completed, the deployed is 0, but finished is 100
	# Solution: remove deployed Dir - (what about naming!)
	# If scripts break, they will be deployed but not marked as done (with done.txt file)
#	numJobletsDeployed=`parallel ssh -oBatchMode=yes -o ConnectTimeout=10 {} "ls GP/$jobName/ \| grep rep" ::: ${servers[@]} 2> /dev/null | wc -l`
	numJobletsDeployed=`parallel ssh -oBatchMode=yes -o ConnectTimeout=10 {} "ps faux \| grep $jobName \| grep -v grep" ::: ${servers[@]} 2> /dev/null | wc -l`

#	jobNameTokens=`echo $jobName | tr '-' ' '`
#	numGPRunsRequired=$(for line in ${tokens[@]} ; do if [ `echo $line | grep Gen ` ] ; then echo $line ; fi ; done | tr 'Gen' ' ')
#	finishedSearchText="eneration: $((numGPRunsRequired-1))" # not really needed, as only jobs finished are retrieved..
#	jobletsFinished=`grep -l "$finishedSearchText" archive/$jobName/rep*/*nfo.log | wc -l`
	jobletsFinished=`find archive/$jobName/rep*/*nfo.log | wc -l`

	# How many jobs required?
	numJobletsToStart=$((numRunsRequired-numJobletsDeployed-jobletsFinished))
        # echo "Need $numJobletsToStart jobs for $jobName"

	echo "$numJobletsDeployed jobs running, $jobletsFinished jobs finished, $numJobletsToStart needed for $jobName"

	if [ "$numJobletsToStart" -gt "0" ]
	then
            # What machines are free? 
	    . ./10-checkFreeServers.sh # exports $freeServers
	    . ./07-deployScripts.sh ${freeServers[@]}
	    if [ -n "${freeServers[0]}" ]
	    then
#		echo "Found: ${#freeServers[@]} free servers"
 
		echo "Deploying to: ${freeServers[@]:0:$numJobletsToStart}"
	        # Deploy jobs required on free machines
  # TODO divide the number required, by the number of free servers, then get each server to run the process x times, instead of 1 time
		parallel ./20-deployServerDir.sh $jarFile {} GP/$jobName 1 ::: "${freeServers[@]:0:$numJobletsToStart}"
      sleep 60
	    else
		echo "No free servers for $jobName, exiting."
		exit
	    fi
	fi
    fi
#    echo `date` "Done checking: $aJob"
done 3< $runningJobsQueue
