#!/bin/bash

analysisDir=$1

# Print out the average, min and max size of individuals
# ./10-stats-getMinMaxAvgFileSize.sh

echo -e "\nGenerations:"
cat $analysisDir/*nfo.log | grep eneration

echo -e "\nFitness distribution for the last population:"
tac $analysisDir/*nfo.log | grep -B 10000 -m1 eneration | grep = | awk '{ print $10 }' | sort -nr | uniq -c

echo -e "\nFitness distribution for initial population:"
cat $analysisDir/*nfo.log | grep -A 10000 -m1 eneration | grep = | awk '{ print $10 }' | sort -nr | uniq -c

echo -e "\nFitness distribution for all generations:"
cat $analysisDir/*nfo.log | grep = | awk '{ print $10 }' | sort -nr | uniq -c


