#!/bin/bash

# run a jar program in a new directory

jarFile="$1"
java6fullpath="/usr/lib/jvm/java-6-openjdk-amd64/jre/bin/java"
jobName=$(echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g')
curDir="$(pwd)"

for probNum in $(seq 0 11)
do 
  for biasNum in $(seq 0 2)
  do
    workingDir="$curDir/$jobName/rep$i-prob$probNum-bias$biasNum-`date +"%Y%m%d%H%M%S"`"
    mkdir -p $workingDir
    cd $workingDir
    $java6fullpath -server -Xss500M -Xmx3072M -XX:ReservedCodeCacheSize=256M -XX:+UseCodeCacheFlushing -XX:MaxPermSize=3072M -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -jar $curDir/$jarFile $probNum $biasNum
#>  /dev/null 2> errorCrap.txt & 
    cd $curDir
  done
done
