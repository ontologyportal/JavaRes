#!/bin/bash

for file in $1/*.p
do
  echo "operate on $file"
  timeout 30 python3 /home/apease/workspace/PyRes/pyres-fof.py --silent "$file"
done