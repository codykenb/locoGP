#!/bin/bash

# this file takes in -GenerationInfo.log file (V2)
# extracts only the fitnesses that exist in that particular generation
# Does NOT need a -GPrun.log file

# Nov 2014. New version of this file to prevent extra blank lines in the file #hackyhack

genCount=0
fileName="genDataFit"$genCount".txt"

if [ -f "$fileName" ] 
then 
    echo -ne "Skipping genDataFit creation for `pwd` ($fileName exists) "
else
    echo -ne "Creating genDataFit txt files for `pwd` "
    echo "" > $fileName
    if [ -f "$1" ]
    then
	while read line
	do
	    mentionCount=`echo $line | grep -n Generation`
	    if [ -z "$mentionCount" ]
	    then
		fitness=`echo $line | grep \= | awk '{ print $10}' `
		echo $fitness >> $fileName
	    else
		genCount=`echo $line | awk '{ print $4}'` #$(($genCount+1))
		fileName="genDataFit"$genCount".txt"
		echo "" > $fileName
	    fi
	done < $1
    else
	echo -ne " Something wrong, file missing "
    fi
fi

# remove blank lines from all files, (these will not be in correct time order afterwards, sigh)
sed -i '/^ *$/d' gen*txt

#genCount=-1
#fileName="genDataFit.txt"

#while [ -f "$fileName" ]
#do
#    sed -i '/^ *$/d' $fileName
####    sleep 1
#    genCount=$((genCount+1))
#    fileName="genDataFit"$genCount".txt"
#done
