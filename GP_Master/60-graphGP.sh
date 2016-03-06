#!/bin/bash

# Author: Brendan Cody-Kenny 4-May-2014

runningJobsQueue=$1 # graph individual runs, while we're waiting for the full number of jobs to come in
finishedJobsQueue=$2 # give an overall view of a finished job
graphableJobs=$3 # Do comparison graphs
graphedJobs=$4 # put graphed jobs here, ready for reporting

baseResultsDir="archive"

# ------------------------------------------------------------- Graph any joblets completed
# Individual graphs for each GP run ("Joblet") can be graphed to show how it performed
while read -u 3 aJob
do
    if [ -n "$aJob" ]
    then
	jarFile=`echo $aJob | awk '{ print $2 }'`
        jobName=`echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g'`
	jobRunRootDir="$baseResultsDir/$jobName/"
	jobRunDirs=( `ls $jobRunRootDir 2> /dev/null` )
	#
	for singleJobRun in ${jobRunDirs[@]}
	do
	    if [[ ! -f  "$jobRunRootDir$singleJobRun/graph.png" ]] # if we haven't already graphed this job individually, have at it.
	    then
		./90-graphSingleGPRun.sh $jobRunRootDir$singleJobRun
	    fi
	    if [[ ! -f  "$jobRunRootDir$singleJobRun/ScatterMinMeanStdDev.pdf" ]] # teething problems, added afterwardsb
	    then
                ./90-graphSingleGPRun.sh $jobRunRootDir$singleJobRun
            fi
	done
	
    fi
done 3< <(cat $runningJobsQueue && cat $finishedJobsQueue) # reacreate any missing graphs in finished queue

# ------------------------------------------------------------- Graph comparing any full jobs completed
# These "jobs" show how many GP runs to compare. Draw the overall average, of all min values across runs.
# get the min (best) value for each gen, for each run, then get the average of these vals per gen, across multiple runs
# idea is that a GP run is repeated many times (e.g. 100) and two or more sets can be compared.
# want to show a graph of these runs overlayed, each line represents a comparison
origDir=`pwd`

while read -u 3 aJob
do

    finished=true # 0=true in bash! If one one of the jobs is not finished, don't bother trying to graph
    
    cd $baseResultsDir
    origJobLine="$aJob"
    if [ -n "$aJob" ]
    then
	jobList=()
	outFileName=""
        newestJob=""
	while [ -n "$aJob" ]
	do
	    numRuns=`echo $aJob | awk '{ print $1 }'`
            jarFile=`echo $aJob | awk '{ print $2 }'`
	    jobName=`echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g'`
	    jobletsFinished=`find archive/$jobName/rep*/*nfo.log 2>/dev/null | wc -l `
	    if [ "$jobletsFinished" -lt "$numRuns" ]
	    then
		finished=false # 1=false in bash, ie error
	    fi
	    outFileName+=$jobName"-"
	    jobList+=( "$jobName" )
	    tempJob=$(echo $aJob | awk '!($1="")')
	    aJob=$(echo $tempJob | awk '!($1="")')
	    if test "$jobName" -nt "$newestJob" ; then newestJob=$jobName ; fi
	done
	#if $finished 
	#then
	# Do the graphing anyway, so we have something to look at, even if its not finished...
	    
	    if test "$newestJob" -nt "$outFileName""vs.pdf"
	    then
		outFileName+="VS.R"
		$origDir/./90-graphAvgMinComparison.sh ${jobList[@]} > $outFileName
#	    mv .RData `date +"%Y%m%d%H%M%S"`.RData 2> /dev/null
   R CMD BATCH $outFileName # use this for debugging, to gather all R data
#		R --no-restore --no-save CMD BATCH $outFileName
	    fi
	#fi
    fi
#    cd $baseResultsDir

    cd $origDir
    if $finished # if we're done, stop graphing the sucka
    then
	echo "`date` $origJobLine" >> GP-Queue-Graphed.txt
	sed -i "/^$origJobLine/d" $graphableJobs
	sed -i '/^$/d' $graphableJobs
    fi
done 3< $graphableJobs


# ------------------------------------------------------------- Graph number of unique individuals per Gen
