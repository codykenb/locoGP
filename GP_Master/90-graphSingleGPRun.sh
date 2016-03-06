
#!/bin/bash

# This file expects the directory containing results of what should be graphed. 

# Hint: for proDir in `find GP54* -type d` ; do 90-graphMyGP.sh $proDir & ; done


origDir=`pwd`
rawDir=$1

# Find the latest log file
echo "Changing to $rawDir "
cd $rawDir

latestInfoLog=`ls -tr *nfo.log| tail -n 1`
latestRunLog=`ls -tr *run.log| tail -n 1`

if [[ ! -f $latestInfoLog ]]
then
    echo "Something wrong, missing info file"
fi

# Generate data from the GP run output
if [[ ! -f allfitness.txt && -f $latestInfoLog ]] 
then
#  echo "Generating data from log file $latestInfoLog"
  $origDir/./71-getGenFitness.sh $latestInfoLog

  ## Get the filenames in the right order
  genCount=-1
  fileName="genDataFit0.txt"
  while [ -f "$fileName" ]
  do
# echo "ps: $pasteString"
      pasteString=$pasteString" "$fileName
      genCount=$((genCount+1))
      fileName="genDataFit"$genCount".txt"
#    echo "fn: $fileName"
  done

  # Generate a graph from this data
#  echo "Collating data $pasteString"
  paste $pasteString > allfitness.txt
fi


echo -ne "Graphing with R: "
R CMD BATCH $origDir/createGraph.R # The file this generates is used as a marker for "already graphed" :/
R CMD BATCH $origDir/90-CreateScatterMinMeanSTDDevPlot.R
echo "done."

cd $origDir
