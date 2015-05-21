#!/bin/bash

# Finds the number of programs that are identical to the original
for genFile in `ls -tr genData* | grep -v Fit`
do 
  while read line 
  do     
    line=`echo $line | awk '{ print $3}'`
    #echo " here it is: $line -------------------------------------------------"
    if [ -a java/$line.java ] 
    then 
        #diff -q -B <(cat java/$line.java  | grep -B 30 -m 1 ^} | egrep -v "Sort1Pr|Orig" ) <(cat /home/bck/GP/OriginalBubble.java| egrep -v "Orig" ) 
	if cmp -s <(cat java/$line.java  | grep -B 30 -m 1 ^} | egrep -v "Sort1Pr|Orig" ) <(cat /home/bck/GP/OriginalBubble.java| egrep -v "Orig" )
	then
	    echo $line.java
	fi
    fi 
  done < $genFile | wc -l
done

# Searches for any optimised lines
#for javaFile in `find $1/*.java`
#do
#    diff -q -B <(cat $javaFile  | grep -B 30 -m 1 ^} | egrep -v "Sort1Pr|Orig" ) <(cat /home/bck/GP/OptimisedBubble.java| egrep -v "Orig" )
#done