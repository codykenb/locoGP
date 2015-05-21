#!/bin/bash

# Takes numerous directories, creates an R script to compare the average of best fitnesses

gpVersions=( $@ )
#baseResultsDir="archive"
origDir=`pwd`

outFileName=$(for dir in ${gpVersions[@]}; do echo -n $dir- ; done)"vs"
#echo "Drawing comparison graph for ${gpVersions[@]}"

# ----------------------------------- generate R script

echo "n.sim<-1000"
echo "library('reshape2')"

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
	echo "if(max("$gpVerTrimmed$fCount"min)>1) stop(\"Found value larger than 1 in $resultFile\")"
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
    echo $gpVerTrimmed"allminCIUI <- apply(rbind("$gpVerTrimmed"allminCI,"$gpVerTrimmed"allminAvg),2, sum)"
    echo $gpVerTrimmed"allminCILI <- apply(rbind("$gpVerTrimmed"allminCI,"$gpVerTrimmed"allminAvg),2, function(x) x[2]-x[1])"

## bootstrapping code for difference between results

    echo $gpVerTrimmed"allminBoot <- matrix(nrow=n.sim,ncol=ncol("$gpVerTrimmed"allmin))"
    echo "for (i in 1:ncol("$gpVerTrimmed"allmin)){ "
    echo "  for (j in 1:n.sim){"
    echo $gpVerTrimmed"allminBoot[j,i] <- mean(sample(melt("$gpVerTrimmed"allmin[,i])\$value, size=1000, replace=T))"
    echo "  }"
    echo "  }"
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
echo "par(bty = 'n')"
echo "plot(seq(0,length("$gpVerTrimmed"allminAvg)-1,1),"$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c(0), ylim=ylimvals, col='white', yaxt='n')"

# draw CI grey
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "polygon(c(0:100,100:0),c("$gpVerTrimmed"allminCIUI, rev("$gpVerTrimmed"allminCILI)), border=NA, col=rgb(.8, .8, .8,0.5))"
done

for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "lines(seq(0,length("$gpVerTrimmed"allminCIUI)-1,1),"$gpVerTrimmed"allminCIUI, lty=2, col='red')"
    echo "lines(seq(0,length("$gpVerTrimmed"allminCILI)-1,1),"$gpVerTrimmed"allminCILI, lty=2, col='red')"
done

# draw the graph lines
pch=0
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "points(seq(0,length("$gpVerTrimmed"allminAvg)-1,1),"$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c("$pch"))"
    echo "lines(seq(0,length("$gpVerTrimmed"allminAvg)-1,1),"$gpVerTrimmed"allminAvg, type=\"c\")"
    pch=$(( $pch + 1 ))
#    echo "title(main=\"$gpVer\")"
#    echo "mtext(\"Minimum (best) values for multiple runs, averaged\")"
#    echo "lines("$gpVerTrimmed"allminAvg, type=\"c\")"
done
#echo "par(mar=c(0, 0, 0, 0))"
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
echo "mtext(\"Fitness\", side=2, line=1)"
echo "axis(2, pos=0)"
echo "dev.off()"
#cd $origDir

############ Copy and pasted from above to produce tex graph ########################

echo "library('tikzDevice')"
echo "tikz(\""$outFileName".tex\",width=6.5,height=5)" 
#echo "pdf(\""$outFileName".pdf\",width=12,height=9)" 
gpVerTrimmed=`echo ${gpVersions[0]} | tr -d '-' `
echo "par(bty = 'n')"
echo "plot(seq(0,length("$gpVerTrimmed"allminAvg)-1,1),"$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c(0), ylim=ylimvals, col='white', yaxt='n')"

# draw CI grey
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "polygon(c(0:100,100:0),c("$gpVerTrimmed"allminCIUI, rev("$gpVerTrimmed"allminCILI)), border=NA, col=rgb(.8, .8, .8,0.5))"
done

for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "lines(seq(0,length("$gpVerTrimmed"allminCIUI)-1,1),"$gpVerTrimmed"allminCIUI, lty=2, col='red')"
    echo "lines(seq(0,length("$gpVerTrimmed"allminCILI)-1,1),"$gpVerTrimmed"allminCILI, lty=2, col='red')"
done

# draw the graph lines
pch=0
for gpVer in "${gpVersions[@]}" # all remaining elements in the array are done with points
do
    gpVerTrimmed=`echo $gpVer | tr -d '-' `
    echo "points(seq(0,length("$gpVerTrimmed"allminAvg)-1,1),"$gpVerTrimmed"allminAvg, xlab=\"Generation\", ylab=NA, pch=c("$pch"))"
    echo "lines(seq(0,length("$gpVerTrimmed"allminAvg)-1,1),"$gpVerTrimmed"allminAvg, type=\"c\")"
    pch=$(( $pch + 1 ))
#    echo "title(main=\"$gpVer\")"
#    echo "mtext(\"Minimum (best) values for multiple runs, averaged\")"
#    echo "lines("$gpVerTrimmed"allminAvg, type=\"c\")"
done
#echo "par(mar=c(0, 0, 0, 0))"
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
echo "mtext(\"Fitness\", side=2, line=1)"
echo "dev.off()"

#
# - --------------------------------------- Differencing graphs (mostly a copy of code above)
#


gpVerTrimmedA=`echo ${gpVersions[0]} | tr -d '-' `
gpVerTrimmedB=`echo ${gpVersions[1]} | tr -d '-' `

echo $gpVerTrimmedB$gpVerTrimmedA"BootDiff <- "$gpVerTrimmedB"allminBoot - "$gpVerTrimmedA"allminBoot"

echo $gpVerTrimmedB$gpVerTrimmedA"BootQB <- unlist(melt(apply("$gpVerTrimmedB$gpVerTrimmedA"BootDiff, 2, quantile,c(0.025)) ))"
echo $gpVerTrimmedB$gpVerTrimmedA"BootQM <- unlist(melt(apply("$gpVerTrimmedB$gpVerTrimmedA"BootDiff, 2, quantile,c(0.5)) ))"
echo $gpVerTrimmedB$gpVerTrimmedA"BootQT <- unlist(melt(apply("$gpVerTrimmedB$gpVerTrimmedA"BootDiff, 2, quantile,c(0.975)) ))"

echo "pdf(\""$outFileName"-BootDiff.pdf\",width=12,height=9)" 
echo "ylimvals <- c(min("$gpVerTrimmedB$gpVerTrimmedA"BootQB),max("$gpVerTrimmedB$gpVerTrimmedA"BootQT))"
echo "par(bty = 'n')"
echo "plot( seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQM)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQM , xlab=\"Generation\", ylab=NA, pch=c(0), ylim=ylimvals, col='white', yaxt='n')"
echo "polygon(c(0:100,100:0),c("$gpVerTrimmedB$gpVerTrimmedA"BootQT, rev("$gpVerTrimmedB$gpVerTrimmedA"BootQB)), border=NA, col=rgb(.8, .8, .8,0.5))"
echo "lines(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQT)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQT, lty=2, col='red')"
echo "lines(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQB)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQB, lty=2, col='red')"
echo "points(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQM)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQM, xlab=\"Generation\", ylab=NA, pch=c(1))" 
echo "lines(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQM)-1,1),"$gpVerTrimmedB$gpVerTrimmedA"BootQM, type=\"c\")"
echo "mtext(\"Fitness\", side=2, line=1)"
echo "axis(2, pos=0)"
echo "lines(c(0, 100), c(0,0), col=\"grey10\", lty=\"dotted\")"
echo "legend('top',c(\""$gpVerTrimmedA"\"),bty='n')"
echo "legend('bottom',c(\""$gpVerTrimmedB"\"),bty='n')"
echo "dev.off()"      




echo "tikz(\""$outFileName"-BootDiff.tex\",width=6.5,height=5)"
echo "ylimvals <- c(min("$gpVerTrimmedB$gpVerTrimmedA"BootQB),max("$gpVerTrimmedB$gpVerTrimmedA"BootQT))"
echo "par(bty = 'n')"
echo "plot( seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQM)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQM , xlab=\"Generation\", ylab=NA, pch=c(0), ylim=ylimvals, col='white', yaxt='n')"
echo "polygon(c(0:100,100:0),c("$gpVerTrimmedB$gpVerTrimmedA"BootQT, rev("$gpVerTrimmedB$gpVerTrimmedA"BootQB)), border=NA, col=rgb(.8, .8, .8,0.5))"
echo "lines(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQT)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQT, lty=2, col='red')"
echo "lines(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQB)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQB, lty=2, col='red')"
echo "points(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQM)-1,1), "$gpVerTrimmedB$gpVerTrimmedA"BootQM, xlab=\"Generation\", ylab=NA, pch=c(1))" 
echo "lines(seq(0,length("$gpVerTrimmedB$gpVerTrimmedA"BootQM)-1,1),"$gpVerTrimmedB$gpVerTrimmedA"BootQM, type=\"c\")"

echo "mtext(\"Fitness\", side=2, line=1)"
echo "axis(2, pos=0)"
echo "lines(c(0, 100), c(0,0), col=\"grey10\", lty=\"dotted\")"
echo "legend('top',c(\""$gpVerTrimmedA"\"),pch=c(1),bty='n')"
echo "legend('bottom',c(\""$gpVerTrimmedB"\"),pch=c(1),bty='n')"
echo "dev.off()"      