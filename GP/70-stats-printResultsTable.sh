#!/bin/bash

function printTable {   
#    tableFile=$1
#fullRows=$(cat $tableFile | grep GP | awk -F\- '{$NF=""; print $0" : " }' )
    columns=$(cat $tableFile | grep GP | awk -F\- '{ print $2 }' | sort  | uniq )
#rows=$(cat $tableFile | grep GP | awk -F\- '{$2="";$1=""; print $0" : " }' | sort  | uniq)
    uniqueRows=$(cat $tableFile | grep GP | awk -F\- '{$2="";$1=""; print $0 }' | awk '{$NF=""; print $0"  :  "}'| sort  | uniq )

#    echo $uniqueRows

    columnCount=$(echo $columns | wc -l)

    echo -en " |    | "

    for experiment in ${columns[@]} 
    do 
	echo -n " $experiment | " 
    done
    
# for each unique row, just settings
    
    echo  " " 
    
    SAVEIFS=$IFS
    IFS=:
    for row in $uniqueRows # for each row, go through all columns
    do
	IFS=$SAVEIFS
	echo -en " | "$(echo $row|tr '\n' ' ')"  | "
	for column in ${columns[@]}
	do
	    unset valueArr
	    searchRow=$(echo $row | sed 's/\ /-/g')
	    valueArr=( $(cat $tableFile | grep "\-$column\-" | grep "$searchRow" | grep GP) )
#	echo "Value array $valueArr"
#	rowArr=( `echo $row` )
	    
	    length=${#valueArr[@]}
	# if length > 0
	    if [ "$length" -gt 0 ]
	    then
		value=${valueArr[$length-1]}
		echo -n " $value | " 
	    else
		echo -ne "           |"
	    fi
#	unset rowArr[$length-1]
	done
	echo " "
	IFS=:
    done
    IFS=$SAVEIFS
}


function generateAvgFitFile {
# Get a list of the best individuals for all runs (not fair as some have more time than others)
    tableFile="avgFit$revisionToCheck.txt"
    for dir in `ls archive | grep $revisionToCheck | grep -vi vs` 
    do 
	echo -n $dir" " 
	for file in `ls archive/$dir/*/*nfo.log`
	do
	    cat $file | grep \= | awk '{ print $10 }' | sort -nr | tail -n1 
	done | awk '{ sum += $1 } END {if (NR >0 ) print sum/NR }'
    done > $tableFile
}

function generateBestFile {
    tableFile="bestFit$revisionToCheck.txt"
    for dir in `ls archive | grep $revisionToCheck | grep -vi vs` ; do echo -n $dir" " ; cat archive/$dir/*/*nfo.log | grep \= | awk '{ print $10 }' | sort -nr | tail -n1; echo " " ; done > $tableFile
}


# #################################################################### Main starts here
# Prints out a table of all the problems, and the best individuals found under different settings

revisionToCheck="GP110"

#echo "Best fitness for revision $revisionToCheck"
#generateBestFile
#printTable

echo "Average fitness for revision $revisionToCheck"
generateAvgFitFile
printTable

exit 0






columnCount=$(echo $columns | wc -l)

echo -n "|    | "

for experiment in ${columns[@]} 
do 
    echo -n " $experiment |" 
done

#for experiment in ${columns[@]}
#do 
#    cat bestList.txt | grep \\\-$experiment- 
#    echo " " 
#done

while read line
do  

    searchPattern=$(echo $line | awk '{ print $1 }')
    unset rows
    rows=($(cat $tableFile | awk '{ print $1 }' | grep $searchPattern))
#    echo "=================searching through $rows  ============================================================="
    for token in $line
    do

	newRows=($(echo ${rows[@]} | grep $token))
	rows=(${newRows[@]})
#	echo "------------------ down to $rows  ------------------"
    done
    echo " "
    echo ${rows[@]}
done < <(cat $tableFile | grep GP | awk '{ print $1}' | awk -F\- '{ print $3" "$6" "$7" "$8}' | sort  | uniq)

