#!/bin/bash

for file in $1/*.p
do
  echo "operate on $file"
  timeout 300 python3 /home/apease/workspace/PyRes/pyres-fof.py -tifbp -HPickGiven5 -nlargest --silent $file
done