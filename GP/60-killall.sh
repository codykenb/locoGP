#!/bin/bash

. ./10-setServers.sh
parallel -j50 ssh {} "killall screen \| killall -9 java" ::: ${servers[@]}
