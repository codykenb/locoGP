#!/bin/bash

# This script should extract the code for a particular program from a *runCode.log file when given the filename

# expects a GPrunCode.log file



cat $1 | grep -A50 "$2\ " | grep -B50 -m 1 "^}" | grep -A50 sort > $2.java
