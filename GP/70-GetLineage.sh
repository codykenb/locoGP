#!/bin/bash

# This program takes the name of a file (20130128-122446-GPrun.log) and finds the first instance of the best program. 
# From this program it traces back through the file to find that programs lineage all the way back to the first generation

logFileName=$1

bestFit=`(cat $logFileName | awk '{ print $9 }' | grep Fit | awk -F: '{ print $2 }' ; cat $logFileName | awk '{ print $10 }' | grep Fit | awk -F: '{ print $2 }' ) | sort -nr | uniq | tail -n 1`

bestFitDetail=`cat $logFileName | grep -m 1 $bestFit`

# define a function that looks up the first occurance of a program, finds its parents and calls the function on each parent
findParents(){
    local offspringName=$1
#    echo -e "\n Looking for parents of: $offspringName"
    local parentLine=`cat $logFileName | grep -m 1 "= $offspringName "`
    local crossover=`echo $parentLine | grep -c +`

    echo "$parentLine"
    
    local seed=`echo $parentLine | grep -c Seed`
    if [ "$seed" -eq "0" ] && [ -n "$parentLine" ]   
    then
	
	if [ "$crossover" -eq "1" ]
	then # crossover occurred, two parents
	# was mutation actually skipped?
	    crossoverSkipped=`echo $parentLine | grep -c Skipped`

	    if [ "$crossoverSkipped" -eq "1" ]
	    then # special case, find out the first parent and follow its lineage (the second parent is not used at all)
		local parentOne=`echo $parentLine | awk '{ print $5 }'`
		if [ -n "$parentOne" ]
		then
		    findParents $parentOne
		fi
	    else 
#		echo -e "\n parentLine = $parentLine"
		local parentOne=`echo $parentLine | awk '{ print $3 }'`

# This gets repetative as the parents overlap (stop search if we've already found this lineage)
#		local parentTwo=`echo $parentLine | awk '{ print $5 }'`
		if [ -n "$parentOne" ]
		then
		    findParents $parentOne
		fi
		if [ -n "$parentTwo" ]
		then
		    echo -e "\n Parent Two - $parentTwo"
		    findParents $parentTwo
		fi
	    fi
	else # mutation occurred, only one parent so
	    local parentOne=`echo $parentLine | awk '{ print $4 }'`
	    if [ -n "$parentOne" ]
	    then
		findParents $parentOne
	    fi
	fi
	
#	echo -e "\nHit the root of the tree"
    fi
}

# loop, for each program name, find parent(s) and call the function on that one also
goldenBoy=`echo $bestFitDetail | awk -F= '{ print $2 }' | awk '{print $1}'`
findParents $goldenBoy

