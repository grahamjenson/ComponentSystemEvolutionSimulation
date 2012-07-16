#!/bin/bash

#an experiment trying to identify the best length for most effective 
#create sv for [7,14,21,28] days

#Simulated Real Users
#Extreme users
#  High install 0.42159     0.7638915 
# lowchange  0.18629183  0.08623483
#  highupdate 0.5610895   0.1918915 
#  mediumchange 0.22202257  0.25948714



for i in `seq 1 50`; 
do 
./gUser.py -u 0.42159 -i 0.7638915 -U"-removed,-new,-stableversion(2419200)" -o q6/conservativehighinstall-$i.user
./gUser.py -u 0.18629183 -i 0.08623483 -U"-removed,-new,-stableversion(2419200)" -o q6/conservativelowchange-$i.user
./gUser.py -u 0.5610895 -i 0.1918915 -U"-removed,-new,-stableversion(2419200)" -o q6/conservativehighupdate-$i.user
./gUser.py -u 0.22202257 -i 0.25948714 -U"-removed,-new,-stableversion(2419200)" -o q6/conservativemediumchange-$i.user

#./gUser.py -u 0.42159 -i 0.7638915 -U"-ipp,-removed,-uptodatedistance,-new" -o q6/progressivehighinstall-$i.user
#./gUser.py -u 0.18629183 -i 0.08623483 -U"-ipp,-removed,-uptodatedistance,-new" -o q6/progressivelowchange-$i.user
#./gUser.py -u 0.5610895 -i 0.1918915 -U"-ipp,-removed,-uptodatedistance,-new" -o q6/progressivehighupdate-$i.user
#./gUser.py -u 0.22202257 -i 0.25948714 -U"-ipp,-removed,-uptodatedistance,-new" -o q6/progressivemediumchange-$i.user
done




