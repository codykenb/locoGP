#!/bin/bash

targetDir=$1

cd $targetDir

echo "--------------------------number of programs which found an improvement ----------------------------"
grep -m 1 =\ 0\\. rep*/*nfo.log

echo -e '\n'Number of runs: `ls | grep -c rep`'\n'

echo "--------------------------number of lines of code for entire generations----------------------------"
for resultsDir in `ls | grep rep`
  do
    cd $resultsDir
      echo $resultsDir
      
      # Find the average file size in LOC and ASTnodes
      for file in `ls Sort*` ; do grep -B 100 -m1 \^} $file | wc -l ; done > fileSizeList.txt
      cat fileSizeList.txt | sort -n | uniq -c > FileSizeDist.txt
      echo ' 'Avg LOC: `awk '{a+=$1} END{print a/NR}' fileSizeList.txt`
      echo ' 'Std Dev: `awk '{sum+=$1; sumsq+=$1*$1} END {print sqrt(sumsq/NR - (sum/NR)^2)}' fileSizeList.txt`
      
      echo ' 'Avg ASTNodes: `grep GPNodes *ionInfo.log | awk '{a+=$14} END{print a/NR}'`
      echo ' 'Std Dev: `grep GPNodes *ionInfo.log |awk '{sum+=$14; sumsq+=$14*$14} END {print sqrt(sumsq/NR - (sum/NR)^2)}'`

      # Find number of unique files
      for file in `ls Sort*` ; do grep -B 100 -m1 \^} $file | grep -v Sort | md5sum ; done > fileHashes.txt
      cat fileHashes.txt | sort | uniq -c >UniqueFileHashes.txt
      echo ' 'Number of unique files: `cat UniqueFileHashes.txt | wc -l`

      # How much bias is in the system?

      # How many retries?
      echo ' 'Programs Gens/count/files: `grep Sort  *nfo.log | awk '{ print $3 }' | awk -Ft '{ print $3 }' | sort -n  | wc -l`/`grep Sort  *run.log | awk '{ print $3 }' | awk -Ft '{ print $3 }' | sort -n  | wc -l`/`ls *.java | wc -l`

      # Actual mutation rate ( mutations applied which resulted in programs in the next population)

      # Actual xover rate

      # Find bias distribution
    cd ..
  done

