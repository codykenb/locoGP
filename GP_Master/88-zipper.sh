#!/bin/bash
export GZIP=-9
for dir in `ls | grep ^GP | grep -v tgz` ; do tar -czvf `hostname`-$dir.tgz $dir --remove-files ; rmdir -p $dir ; done
