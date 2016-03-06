#!/bin/bash

# Takes numerous directories, creates an R script to compare the average of best fitnesses

gpVersions=( $@ )
#baseResultsDir="archive"
origDir=`pwd`

outFileName=$(for dir in ${gpVersions[@]}; do echo -n $dir- ; done)"vs"
#echo "Drawing comparison graph for ${gpVersions[@]}"

# ----------------------------------- generate R script

# This loop reads in data, and prepares the necessary arrays for drawing the graph lines
for gpVer in "${gpVersions[@]}" # These are results directories ( which each contain many run repition directories )
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    finalString=$gpVerTrimmed"allmin <- rbind("
    fCount=0
    # get results files from all repition directories
    resultFileList=`find $gpVer/rep*/allfitness.txt`
    for resultFile in $resultFileList
    do 
#	echo "$gpVer$fCount <- read.table(file=\"$resultFile\", header=FALSE, nrows = 250 )" 
	echo "$gpVerTrimmed$fCount <- read.table(file=\"$resultFile\", header=FALSE, nrows = 100 )" 
	echo $gpVerTrimmed$fCount"min <- apply($gpVerTrimmed$fCount, 2, min)" 
#	finalString+="head("$gpVer$fCount"min,n=100),"
	if [ "$fCount" -eq "0" ]
	then
	    finalString+=$gpVerTrimmed$fCount"min"
	else
	    finalString+=","$gpVerTrimmed$fCount"min"
	fi
	((fCount++)) 
    done
    
    finalString+=")"
    echo $finalString 
    echo $gpVerTrimmed"allminAvg <- apply("$gpVerTrimmed"allmin, 2, mean)"
    echo $gpVerTrimmed"allminCI <- apply("$gpVerTrimmed"allmin,2, function(x) qnorm(0.975)*sd(x)/sqrt(length(x)) )"
    echo $gpVerTrimmed"allminCIUI <- apply(rbind("$gpVerTrimmed"allmin,2, sum))"
    echo $gpVerTrimmed"allminCILI <- apply(rbind("$gpVerTrimmed"allmin,2, function(x) x[2]-x[1]))"
done

#echo "library('tikzDevice')" 
#echo "postscript(\""$outFileName".eps\")" 

# TODO before plotting, get the min/max for all data sets so the graph can accomodate all without wasting space
# find the ylim from all data sets                                                                                                
ylimLine=""
ylimLine+="ylimvals <- c("
gpVerTrimmed=`echo ${gpVersions[0]} | tr -d '-' `
ylimLine+="min("$gpVerTrimmed"allminCILI"
for gpVer in "${gpVersions[@]:1}"
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    ylimLine+=","$gpVerTrimmed"allminCILI"
done
gpVerTrimmed=`echo ${gpVersions[0]} | tr -d '-' `
ylimLine+="),max("$gpVerTrimmed"allminCIUI"
for gpVer in "${gpVersions[@]:1}"
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    ylimLine+=","$gpVerTrimmed"allminCIUI"
done
ylimLine+="))"
echo $ylimLine

echo "pdf(\""$outFileName".pdf\",width=12,height=9)" 
gpVerTrimmed=`echo ${gpVersions[0]} | tr -d '-' `
echo "plot("$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c(0), ylim=ylimvals, col='white', yaxt='n')"

# draw CI grey
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "polygon(c(1:101,101:1),c("$gpVerTrimmed"allminCIUI, rev("$gpVerTrimmed"allminCILI)), border=NA, col=rgb(.8, .8, .8,0.5))"
done

for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "lines("$gpVerTrimmed"allminCIUI, lty=2, col='red')"
    echo "lines("$gpVerTrimmed"allminCILI, lty=2, col='red')"
done

# draw the graph lines
pch=0
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "points("$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c("$pch"))"
    echo "lines("$gpVerTrimmed"allminAvg, type=\"c\")"
    pch=$(( $pch + 1 ))
#    echo "title(main=\"$gpVer\")"
#    echo "mtext(\"Minimum (best) values for multiple runs, averaged\")"
#    echo "lines("$gpVerTrimmed"allminAvg, type=\"c\")"
done
echo "par(mar=c(0, 0, 0, 0))"
echo "legend('topright',c(\"${gpVersions[0]}\""
for gpVer in "${gpVersions[@]:1}"
do
    echo -n ",\"$gpVer\""
done
echo -n "),pch=c(" #'.','o'),y.intersp=1.7)"
echo -n "0"
pch=1
for gpVer in "${gpVersions[@]:1}"
do
    echo -n ",$pch"
    pch=$(( $pch + 1 ))
done
echo -n "),bty='n')" #,y.intersp=1.7)" 
echo " "
echo "axis(2, pos=0)"
echo "mtext('Fitness', side=2, line=1)"
echo "dev.off()"
#cd $origDir

############ Copy and pasted from above to produce tex graph ########################

echo "library('tikzDevice')"
echo "tikz(\""$outFileName".tex\",width=12,height=9)" 
#echo "pdf(\""$outFileName".pdf\",width=12,height=9)" 
gpVerTrimmed=`echo ${gpVersions[0]} | tr -d '-' `
echo "plot("$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c(0), ylim=ylimvals, col='white', yaxt='n')"

# draw CI grey
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "polygon(c(1:101,101:1),c("$gpVerTrimmed"allminCIUI, rev("$gpVerTrimmed"allminCILI)), border=NA, col=rgb(.8, .8, .8,0.5))"
done

for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "lines("$gpVerTrimmed"allminCIUI, lty=2, col='red')"
    echo "lines("$gpVerTrimmed"allminCILI, lty=2, col='red')"
done

# draw the graph lines
pch=0
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "points("$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c("$pch"))"
    echo "lines("$gpVerTrimmed"allminAvg, type=\"c\")"
    pch=$(( $pch + 1 ))
#    echo "title(main=\"$gpVer\")"
#    echo "mtext(\"Minimum (best) values for multiple runs, averaged\")"
#    echo "lines("$gpVerTrimmed"allminAvg, type=\"c\")"
done
echo "par(mar=c(0, 0, 0, 0))"
echo "legend('topright',c(\"${gpVersions[0]}\""
for gpVer in "${gpVersions[@]:1}"
do
    echo -n ",\"$gpVer\""
done
echo -n "),pch=c(" #'.','o'),y.intersp=1.7)"
echo -n "0"
pch=1
for gpVer in "${gpVersions[@]:1}"
do
    echo -n ",$pch"
    pch=$(( $pch + 1 ))
done
echo -n "),bty='n')" #,y.intersp=1.7)" 
echo " "
echo "axis(2, pos=0)"
echo "mtext('Fitness', side=2, line=1)"
echo "dev.off()"

