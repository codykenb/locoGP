#!/bin/bash

java6fullpath="/usr/lib/jvm/java-6-openjdk-amd64/jre/bin/java"

cd ~/GP-testGround

while [ "`ls locoGP-GP11*.jar | wc -l`" -ne 0 ]
do 
    file=`ls -t locoGP-GP11*.jar | tail -n1`
    touch $file
    newDir=/tmp/`echo $file | awk -F. '{ print $1}'``hostname``date +"%Y%m%d%H%M%S"` 
    mkdir $newDir 
    cp $file $newDir 
    cd $newDir 
    $java6fullpath -server -Xss500M -Xmx3072M -XX:ReservedCodeCacheSize=256M -XX:+UseCodeCacheFlushing -XX:MaxPermSize=3072M -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -jar *.jar 
    rm *.jar
    for i in {0..249} 
	do 
	echo $i
	diff Seed-Sort1ProblemTest*-$i.bias Seed-Sort1ProblemTest*-$((i+1)).bias 
    done | grep \> | awk '{ print $2 }' | sort -nr | uniq -c | sort -nr > biasUpdateFrequencyAtLocations.txt 
    tar -czvf javaFiles.tgz *.java *.log --remove-files
    cd ~/GP-testGround
    mv $newDir ~/GP-testGround
done
