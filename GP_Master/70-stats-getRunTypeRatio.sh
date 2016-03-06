#!/bin/bash

problemName=$1
optimisation=$2
threshold=$3

echo "For $problemName we are looking for $optimisation in programs less than $threshold"

echo -ne "Total runs: "
ls $problemName/*/*nfo.log | wc -l

echo -ne "Total Improvements: "
grep -m1 -l =\ $threshold $problemName/*/*nfo.log  | wc -l

# unzip the interesting java files (some are missing due to elitism)
for dir in $(grep -m1 -l =\ $threshold $problemName/*/*nfo.log  | awk -F/ '{ print $1"/"$2 }') 
do 
    cd $dir/ 
    tar -xzvf javaFiles.tgz $(grep =\ $threshold *nfo.log  |awk '{ print $3 }' | awk '{ print $0".java" }' | tr '\n' '\ ') 
    cd - 
done

# search for an optimisation
echo -ne "Opt $optimisation : "
for dir in $(ls $problemName) ; do grep -l "$optimisation"   $problemName/$dir/*.java ; done | awk -F/ '{ print $1"/"$2 }' | sort| uniq | wc -l