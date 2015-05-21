#!/bin/bash

# you need to modify the legend when including the tex file in a paper

# \path[draw=drawColor,line width= 0.4pt,line join=round,line cap=round] (224.99,396.27) rectangle (470.31,326.67);

gpVersionRef="GP83"

gpVersions[0]='nofocus'
gpVersions[1]='R3'
#gpVersions[2]='R2'
#gpVersions[3]='gaussian'


for gpVer in "${gpVersions[@]}"
do
#    echo $gpVer"allmin <- rbind(head(nofoc0min,n=40) , head(nofoc1min,n=40), head(nofoc2min,n=40), head(nofoc3min,n=40))
    
    finalString=$gpVer"allmin <- rbind("

    fCount=0  
    resultFileList=`find $gpVersionRef* | grep all | grep rep | grep $gpVer `
#    for resultFile in `find $gpVersionRef* | grep all | grep rep | grep $gpVer` # get files which have at least 100 rows
    for resultFile in `for file in $resultFileList ; do echo -n $file -n 1 $file | wc ; done | awk '$4>100 {print $1}'` 
    do 
	echo "$gpVer$fCount <- read.table(file=\"$resultFile\", header=FALSE, nrows = 250 )" 
	echo $gpVer$fCount"min <- apply($gpVer$fCount, 2, min)" 
	finalString=$finalString"head("$gpVer$fCount"min,n=100),"
	((fCount++)) 
    done
    
    finalString=`echo "${finalString%?}"`")"
    echo $finalString
    echo $gpVer"allminAvg <- apply("$gpVer"allmin, 2, mean)"
done

echo "library('tikzDevice')"

echo "setEPS()"
echo "postscript(\"GPcomparison"$gpVersionRef".eps\")"
echo "#   tikz( 'GPcomparison"$gpVersionRef".tex' )"
#echo "plot("${gpVersions[0]}"allminAvg, ylim=c(.96,1), xlim=c(0,100), xlab=\"Generation No.\", ylab=\"Fitness (Normalised)\", pch=c('.', rep(NA,2)))"
echo "plot("${gpVersions[0]}"allminAvg, xlab=\"Generation No.\", ylab=\"Fitness (Normalised)\", pch=c('.', rep(NA,2)))"
    echo "lines(seq(0,length("${gpVersions[0]}"allminAvg)-1,1), "${gpVersions[0]}"allminAvg, type=\"c\")"

for gpVer in "${gpVersions[@]}"
do 
    echo "points(seq(0,length("$gpVer"allminAvg)-1,1), "$gpVer"allminAvg, pch=c('o', rep(NA,3)))"
    echo "lines(seq(0,length("$gpVer"allminAvg)-1,1), "$gpVer"allminAvg, type=\"c\")"
done

# legend(40,.99,c("Canonical GP","Self-focusing GP Ruleset 1","Self-focusing GP Ruleset 2"),pch=c('.','o','-'),y.intersp=1.7)
echo "legend(55,.99,c(\"Canonical GP\",\"Self-focusing GP\"),pch=c('.','o'),y.intersp=1.7)"
echo "dev.off()"



echo "   tikz( 'GPcomparison"$gpVersionRef".tex' )"
echo "plot("${gpVersions[0]}"allminAvg, ylim=c(.96,1), xlim=c(0,100), xlab=\"Generation No.\", ylab=\"Fitness (Normalised)\", pch=c('.', rep(NA,2)))"
    echo "lines(seq(0,length("${gpVersions[0]}"allminAvg)-1,1), "${gpVersions[0]}"allminAvg, type=\"c\")"

for gpVer in "${gpVersions[@]}"
do 
    echo "points(seq(0,length("$gpVer"allminAvg)-1,1), "$gpVer"allminAvg, pch=c('o', rep(NA,3)))"
    echo "lines(seq(0,length("$gpVer"allminAvg)-1,1), "$gpVer"allminAvg, type=\"c\")"
done

# legend(40,.99,c("Canonical GP","Self-focusing GP Ruleset 1","Self-focusing GP Ruleset 2"),pch=c('.','o','-'),y.intersp=1.7)
echo "legend(55,.99,c(\"Canonical GP\",\"Self-focusing GP\"),pch=c('.','o'),y.intersp=1.7)"
echo "dev.off()"

