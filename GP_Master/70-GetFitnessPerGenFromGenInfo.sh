#!/bin/bash


# expects a 20120917-094749-GenerationInfo.log type file

genNum=""
outFileName="genFitness$genNum.txt"

while read line
do
    # values=`wc -l src/$genDir/* | grep -v total |  awk '{ print $1 }' `

	mentionCount=`echo $line | grep -n Generation`

	if [ -n "$mentionCount" ]
	then
	genNum=`echo $mentionCount | awk '{ print $4 }'`
	outFileName="genFitness$genNum.txt" 
	echo -n "" > $outFileName
	echo "mention"$mentionCount
	fi

	probMention=`echo $line | grep -n Sort1Problem`

	if [ -n "$probMention" ]
        then
	echo "prob "$probMention
		problemName=`echo $line | awk '{print $3}'`

	    fitLine=`cat *GPrun.log | grep -m1 "= $problemName "`

	    mutResult=`echo $fitLine | grep Mutating`

		if [ -n "$mutResult" ]
		then
			fitness=`echo $fitLine | awk '{ print $9}' | awk -F: '{ print $2}'`
		else

			fitness=`echo $fitLine | awk '{ print $10}' | awk -F: '{ print $2}'`
		fi
	    
		echo "$fitness" >>"$outFileName"

	fi

done < $1
