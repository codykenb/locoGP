#!/bin/bash

analysisDir=$1

# If the file has exactly 100 generations, find out if it found the optimisation or not.

# Print out the average, min and max size of individuals
# ./10-stats-getMinMaxAvgFileSize.sh

# echo -e "\nGenerations:"
cat $analysisDir/*nfo.log | grep eneration | tail -n 1 

cat $analysisDir/*nfo.log | grep = | awk '{ print $10 }' | sort -nr | uniq -c | tail -n1



