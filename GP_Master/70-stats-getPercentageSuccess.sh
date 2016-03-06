#!/bin/bash

source 50-setServers.sh

dirString=$1

# vals=( $(parallel ssh {} grep -m1 -c \\\\=\\\\ 0 $dirString/rep\*/\*nfo.log ::: ${servers[@]} | tee  >(wc -l) >(grep -c 1$) | tail -n2))  ; echo "Calculation is ${vals[0]}/${vals[1]}" ; echo "scale=4 ; ${vals[0]}/${vals[1]}"  | bc

vals=( $(parallel ssh -q {} grep -s -m1 -c \\\\=\\\\ 0 $dirString/rep\*/\*nfo.log ::: ${servers[@]} | tee  >(wc -l) >(grep -s -c 1$) | tail -n2))  ; echo "Calculation is ${vals[0]}/${vals[1]}" ; echo "scale=4 ; ${vals[0]}/${vals[1]}"  | bc

