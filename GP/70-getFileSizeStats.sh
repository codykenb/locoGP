#!/bin/bash

echo "File size"
echo -e "Average \t\t\t Min \t\t\t Max \t\t\t StdDev"
for genDir in `ls -tr src | sort -n -k2 -t"n"`
do
    # values=`wc -l src/$genDir/* | grep -v total |  awk '{ print $1 }' ` 
echo $genDir
    wc -l src/$genDir/*.java | grep -v total |  awk '{ print $1 }' | awk '{ sum+=$1} END { printf "%f",sum/NR}' 
    echo -en " \t\t\t"
    wc -l src/$genDir/*.java | grep -v total |  awk '{ print $1 }' | awk 'min =="" || $1 < min {min=$1} END{ printf "%f",min}'
    echo -en " \t\t"
    wc -l src/$genDir/*.java | grep -v total |  awk '{ print $1 }' | awk 'min =="" || $1 > min {min=$1} END{ printf "%f",min}'
    echo -en " \t\t"
    wc -l src/$genDir/*.java | grep -v total |  awk '{ print $1 }' | awk '{sum+=$1; sumsq+=$1*$1} END {print sqrt(sumsq/NR - (sum/NR)**2)}'

    wc -l src/$genDir/*.java | grep -v total |  awk '{ print $1 }' > src/$genDir/fileSizes.txt
    echo ""
done

paste `ls -tr src/ge*/*.txt` > fileSizesPerGen.txt

