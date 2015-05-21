#!/bin/bash

parallel -j50 scp -o ConnectTimeout=5 88-zipper.sh lg12l{}:GP/ ::: {1..40} 

#sleep 10

parallel -j50 ssh -o ConnectTimeout=5 lg12l{} "chmod +x GP/88-zipper.sh" ::: {1..40} 

# sleep 10

parallel -j50 ssh -o ConnectTimeout=5 lg12l{} "cd GP \; ./88-zipper.sh" ::: {1..40}

cd archive

parallel -j50 rsync --remove-source-files -avz lg12l{}:GP/*.tgz . ::: {1..40}

cd ..
