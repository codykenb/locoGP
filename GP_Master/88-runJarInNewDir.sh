#!/bin/bash

# run a jar program in a new directory

jarFile="$1"
javafullpath=$(which java)
jobName=$(echo $jarFile | sed 's/locoGP-//g' | sed 's/\.jar//g')
curDir="$(pwd)"
workingDir="$curDir/$jobName/rep$i-`hostname`-`date +"%Y%m%d%H%M%S"`"


compressFiles(){
  sleep 600
  biasFileList="$(ls *bias)"
  nice 10 7z -mx9 u biasFiles.7z $biasFileList
  rm $biasFileList
  javaFileList="$(ls *java)"
  nice 10 7z -mx9 u javaFiles.7z $javaFileList
  rm $javaFileList
}


mkdir -p $workingDir
cd $workingDir
$javafullpath -server -Xss32M -Xmx5100M -XX:ReservedCodeCacheSize=1000M -XX:+UseCodeCacheFlushing -XX:+ScavengeBeforeFullGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -jar $curDir/$jarFile $2 &
pid=$!
while ps -p $pid > /dev/null
do
    compressFiles
done

compressFiles

#>  /dev/null 2> errorCrap.txt & 
cd $curDir
