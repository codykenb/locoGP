
#!/bin/bash

# Author: Brendan Cody-Kenny 30-April-2014

# This script is meant to control end-to-end GP job scheduling, 
# a jar file and number of runs is specified and this script 
# should see the process all the way through to producing a number of stats and graphs

newJobsQueue="GP-Queue-New.txt" # each line contains numRunsRequired, jarFile ... (can repeat n times)
runningJobsQueue="GP-Queue-Pending.txt" # each line is numRunsRequired, jarFile
finishedJobsQueue="GP-Queue-Finished.txt" # ready for graphing
graphableJobs="GP-Queue-ForGraphing.txt"
graphedJobs="GP-Queue-Graphed.txt"
experimentLog="GP-Queue-CompletedExperimentLog.txt"


while :
do
    echo -ne "\n <Deployer awake> " 

# 05 ----------------------------------------------- Check for new jobs to run
# How are programs enqueued? (Tell grapher what jobs to compare)
    ./11-queueNewJobs.sh $newJobsQueue $runningJobsQueue $graphableJobs
    
# -------------------------------------------------- Check running jobs
# Check free machines, check running programs, deploy new jobs
    ./20-deployer.sh $runningJobsQueue

    echo -ne " <Deployer asleep> "  
    sleep $[ ( $RANDOM % 300 ) + 100 ]s

done & # This loop runs in background, keeps other machines busy even if this machine is busy drawing graphs



while :
do
#    echo `date` Deploying scripts
# Deploy needed scripts remotely
#    ./07-deployScripts.sh # This has been moved into ./11-queueNewJobs.sh so it only deploys scripts to free servers
    
# 05 ----------------------------------------------- Check for new jobs to run
# How are programs enqueued? (Tell grapher what jobs to compare)
#    ./11-queueNewJobs.sh $newJobsQueue $runningJobsQueue $graphableJobs
    
# -------------------------------------------------- Check running jobs
# Check free machines, check running programs, deploy new jobs
#    ./20-deployer.sh $runningJobsQueue

    echo -ne "\n <Queue & retrieval awake> "  


# -------------------------------------------------- Check for finished jobs
# 50 retrieve
    ./50-retrieveFinished.sh $runningJobsQueue $finishedJobsQueue

#   move local finished jobs to graphing queue
    ./55-moveFullyFinishedToGraphing.sh $runningJobsQueue $finishedJobsQueue

# -------------------------------------------------- Graph 
# analyse/stats, graph
    ./60-graphGP.sh $runningJobsQueue $finishedJobsQueue $graphableJobs $graphedJobs

# -------------------------------------------------- Report
# combine stats & graphs in html or email
#    ./95-GPReport.sh $graphedJobs $experimentLog

    echo -ne " <Queue & retrieval asleep> "  

    sleep $[ ( $RANDOM % 300 ) + 100 ]s
done