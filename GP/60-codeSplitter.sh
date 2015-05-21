#!/bin/bash

outDir="src"

# expects a GPrunCode.log file

#mkdir -p $outDir

genNum=0

genDir="gen"

mkdir -p $outDir/$genDir

outFlag=false
fileName=""

fileCount=0

lineCount=0

old_IFS=$IFS
IFS=

while read line 
  do
    
    genLine=`echo $line | grep Generation | awk '{ print $4 }'`

    if [[ -n $genLine ]]
    then
        echo "Generation "$genLine
	genDir="gen$genLine"
#       outFile=$outDir/$outFilePrefix$genLine.txt
	mkdir -p $outDir/$genDir
#       echo "" > $outFile
	
    fi

  mentionCount=`echo $line | grep class | grep Sort1Problem | grep -n "\{"`
  if [ -n "$mentionCount" ]
    then

      lastMention=$mention
      mention=`echo $line | grep class | grep Sort1Problem` 

    outFlag=true
    fileName=$outDir/$genDir/"Sort1Problem$fileCount.java"
    
    if [ "$lastMention" != "$mention" ]
    then
	fileCount=$(($fileCount+1))
    fi

    echo "Writing $fileName"
    echo -n "" > $fileName
  fi
  
  if $outFlag 
    then
    echo "$line" >> $fileName
    lineCount=$(($lineCount + 1))
  fi

  mentionCount=`echo "$line" | grep -n "^\}"`
  if [ -n "$mentionCount" ]
    then
    outFlag=false
#    echo $lineCount >> "$outFile"
  fi



done < $1

IFS=$old_IFS

# cat genData0.txt | awk '{ print $9 }' | awk -F\: '{ print $2 }' | awk '{ sum+=$1} END { print "Average = ",sum/NR}'
# echo -e "Average \t\t\t Min \t\t\t Max"
# for file in `ls genData*.txt` 
#   do 
#   cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk '{ sum+=$1} END { print "%f",sum/NR}' 
#   echo -en " \t\t\t"
#   cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 < min {min=$1} END{ printf "%f",min}'
#   echo -en " \t\t\t"
#   cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 > min {min=$1} END{ printf "%f",min}'
#   echo " "
# done




#for file in `ls genData*.txt` ;  do cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 < min {min=$1} END{ print "Min = ", min}' ; done
#for file in `ls genData*.txt` ;  do cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 > min {min=$1} END{ print "Max = ", min}' ; done
