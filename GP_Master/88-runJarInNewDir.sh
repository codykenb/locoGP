#!/bin/bash

# run a jar program in a new directory

jarFile="$1"
java6fullpath="/usr/lib/jvm/java-6-openjdk-amd64/jre/bin/java"
jobName=$(echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g')
curDir="$(pwd)"
workingDir="$curDir/$jobName/rep$i-`hostname`-`date +"%Y%m%d%H%M%S"`"


compressFiles(){
  sleep 60
    biasFileList="$(ls *bias)"
  7z -mx9 u biasFiles.7z $biasFileList
  rm $biasFileList
  javaFileList="$(ls *java)"
  7z -mx9 u javaFiles.7z $javaFileList
  rm $javaFileList
}


mkdir -p $workingDir
cd $workingDir
$java6fullpath -server -Xss32M -Xmx5100M -XX:ReservedCodeCacheSize=1000M -XX:+UseCodeCacheFlushing -XX:MaxPermSize=2000M -XX:+ScavengeBeforeFullGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled  -jar $curDir/$jarFile $2 &
pid=$!
while ps -p $pid > /dev/null
do
    compressFiles
done

compressFiles

#>  /dev/null 2> errorCrap.txt & 
cd $curDir
