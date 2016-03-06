#!/bin/bash

outFile="sizeVsFitness.txt"

echo -n "" > "$outFile"
for genDir in `ls -tr src`
do
    # values=`wc -l src/$genDir/* | grep -v total |  awk '{ print $1 }' `
    echo $genDir 
    for file in `ls -tr src/$genDir/`
    do
	    fileSize=`wc -l src/$genDir/$file |  awk '{ print $1 }'` 
	    
	    indFile=`cat src/$genDir/$file | grep Sort1 | awk '{ print $2} '`

	    fitLine=`cat *GPrun.log | grep -m1 "= $indFile "`

	    mutResult=`echo $fitLine | grep Mutating`

		if [ -n "$mutResult" ]
		then
			fitness=`echo $fitLine | awk '{ print $9}' | awk -F: '{ print $2}'`
		else

			fitness=`echo $fitLine | awk '{ print $10}' | awk -F: '{ print $2}'`
		fi
	    
		echo "$fileSize $fitness" >>"$outFile"
#		echo "$indFile $fileSize $fitness"

     done
done
