#!/bin/bash
numRuns=$((50 + $1))
java6fullpath="/usr/lib/jvm/java-6-openjdk-amd64/jre/bin/java"


curDir=`pwd`
for (( i=50; i<$numRuns; i++)) 
do
    workingDir="$curDir/rep$i-`hostname`-`date +"%Y%m%d%H%M%S"`"
    if [ ! -d $workingDir ]
    then
	mkdir $workingDir
	cd $workingDir
# -Xss is the stack size per thread, if its too small, the seed and other programs may not compile. 
# this puts a limit on the depth of method calls in a program (maybe stackoverflow programs should not receive the worst fitness?)
	$java6fullpath -server -Xss500M -Xmx3072M -XX:ReservedCodeCacheSize=256M -XX:+UseCodeCacheFlushing -XX:MaxPermSize=3072M -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -jar ../*.jar >  /dev/null 2> errorCrap.txt & 
	javaPID=$!
  sleep 30
	while [ -n "`ps faux | grep jar | grep loco | grep $javaPID | grep -v grep `" ] # while our jar is running
	do
	    # maybe change who call to "last -f /var/run/utmp"
	    if [ -z "`who`" ] # if noone is logged in, run like mad
	    then
		kill -CONT $javaPID
		sleep 15
	    else # If someone is logged in, pause the process
		kill -STOP $javaPID
		sleep 150
	    fi
	done

	# We're done, zip up all that redundant java
	GZIP=-9
	tar -czvf javaFiles.tgz *.java --remove-files
	tar -czvf errorCrap.tgz errorCrap.txt --remove-files
  tar -czvf biasFiles.tgz *.bias --remove-files
	touch done.txt # This file acts as a marker

	cd $curDir
  else  
      numRuns=$((numRuns + 1))
  fi
done
