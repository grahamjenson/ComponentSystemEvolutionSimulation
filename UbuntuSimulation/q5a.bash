#!/bin/bash

#an experiment trying to identify the best length for most effective 
#create sv for [7,14,21,28] days
for sv in "604800" "1209600" "1814400" "2419200"
do
./gUser.py -u 1  -U"-removed,-new,-stableversion($sv)" -o q5a/alwaysupdate.$sv.user
for i in `seq .2 .2 .8`
do 
for x in `seq 1 5`
do
./gUser.py -u $i -U"-removed,-new,-stableversion($sv)" -o q5a/u$sv.$i.$x.user
done
done
done



