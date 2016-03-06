#!/bin/bash

# Author: Brendan Cody-Kenny 30-April-2014
# Review running jobs, retrieve finished jobs


# TODO Think about removing the directories as they are completed.

#  ./50-retrieveFinished.sh GP-Queue-Pending.txt GP-Queue-Finished.txt
runningJobsQueue=$1
finishedJobsQueue=$2

. ./10-setServers.sh # exports $servers                                                                                                
#echo `date` Checking for finished Joblets
while read -u 3 aJob
do
#    echo `date` "Checking: $aJob"
    if [ -n "$aJob" ]
    then
	numRunsRequired=`echo $aJob | awk '{ print $1 }'`
        jarFile=`echo $aJob | awk '{ print $2 }'`
        jobName=`echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g'`
	
#	finishedSearchText="eneration:\\\ $((numRunsRequired-1))"
#	echo Looking for $finishedSearchText for $jobName
#	jobletsFinished=($(parallel -j100 ssh -o ConnectTimeout=5 {} "grep -l $finishedSearchText GP/$jobName/rep*/*nfo.log \&\& hostname 2> /dev/null " ::: ${servers[@]} ))
	doneMarker='done.txt'
	jobletsFinished=($(parallel -j100 ssh  -oBatchMode=yes -o ConnectTimeout=5 {} "find GP/$jobName/rep*/$doneMarker \&\& hostname 2> /dev/null " ::: ${servers[@]} ))

	numJobletsFinished=$(for line in ${jobletsFinished[@]} ; do echo $line | grep $doneMarker ; done | sed '/^$/d' | wc -l)

	if [ "$numJobletsFinished" -gt "0" ]
	then
	    saveDir="archive/$jobName"
	    mkdir -p $saveDir
#	    echo " "
#	    echo "Retrieving: "
	    for resultLine in ${jobletsFinished[@]} 
	    do 
		fileLocation=$(echo $resultLine | grep $doneMarker)
		if [ -n "$fileLocation" ]
		then # if its a file location, add it to the dirList
		    tempResultDir=`echo $resultLine | awk -F/ '{ $NF = "" ; print }'`
		    resultDir=`echo ${tempResultDir[@]} | awk 'BEGIN { OFS = "/" } { $1 = $1; print }'`
		    dirList+=( $resultDir )
		else # if its not a file, we have a server name, so retrieve all dirs from this server
		    for aDir in "${dirList[@]}"
		    do
			#echo "rsync --remove-source-files -avz $resultLine:$aDir $saveDir/"
			rsync --remove-source-files -avz $resultLine:$aDir $saveDir/
			ssh $resultLine "rmdir $aDir"
		    done
		    unset dirList
		fi
	    done
#	    echo " "
	fi
	
	# If we have enough programs locally, move the job to graphing, cleanup the job remotely

    fi
#    echo `date` "Done checking: $aJob"
done 3< $runningJobsQueue
