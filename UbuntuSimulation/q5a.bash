#!/bin/bash

#an experiment trying to identify the best length for most effective 
#create sv for [7,14,21,28] days
for sv in "604800" "1209600" "1814400" "2419200"
do
./gUser.py -u 1  -U"-removed,-new,-stableversion($sv),-uptodatedistance" -o q5a/alwaysupdate.$sv.user
done



