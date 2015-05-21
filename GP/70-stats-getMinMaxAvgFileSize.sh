#!/bin/bash

echo "For crossover"
echo -e "Average \t\t\t Min \t\t\t Max"

for file in `ls -tr genData*.txt`
  do
  echo $file
# Print out the Average
  cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk '{ sum+=$1} END { printf "%f",sum/NR}'
  echo -en " \t\t\t"

#Print out the minimum
  minVal=9999999
  for fitval in `cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' `
  do
   if [ ` echo $fitval"<"$minVal| bc -l ` ]
   then
      minVal=$fitval
   fi
  done
  echo -n " $minVal "
  cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 < min {min=$1} END{ printf "%f",min}'
  
  echo -en " \t\t\t"
  cat $file | grep + |  awk '{ print $10 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 > min {min=$1} END{ printf "%f",min}'
  echo " "
done

echo "\n\nFor mutation:"
echo -e "Average \t\t\t Min \t\t\t Max"

for file in `ls -tr genData*.txt`
  do
  cat $file | grep Mutating |  awk '{ print $9 }' | awk -F\: '{ print $2 }' | awk '{ sum+=$1} END { printf "%f",sum/NR}'
  echo -en " \t\t\t"
  cat $file | grep Mutating |  awk '{ print $9 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 < min {min=$1} END{ printf "%f",min}'
  echo -en " \t\t\t"
  cat $file | grep Mutating |  awk '{ print $9 }' | awk -F\: '{ print $2 }' | awk 'min =="" || $1 > min {min=$1} END{ printf "%f",min}'
  echo " "
done

