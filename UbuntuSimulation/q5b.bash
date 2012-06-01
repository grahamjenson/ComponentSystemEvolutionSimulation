#!/bin/bash

#an experiment trying to identify the best length for most effective 
#create sv for [7,14,21,28] days
for sv in "604800" "1209600" "1814400" "2419200"
do
for i in `seq 1 30`; 
do 

./gUser.py -u 0.264151 -i 0.358491 -U"-removed,-new,-stableversion($sv)" -o q5b/u1-$sv-$i.user
./gUser.py -u 0.286885 -i 0.204918 -U"-removed,-new,-stableversion($sv)" -o q5b/u2-$sv-$i.user

done
done



