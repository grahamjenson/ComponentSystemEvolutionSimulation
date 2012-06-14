#!/bin/bash

#Simulated Real Users
#Extreme users
#  High install 0.42159     0.7638915 
#  0.18629183  0.08623483
#  0.5610895   0.1918915 
#  0.22202257  0.25948714



for i in `seq 1 50`; 
do 
./gUser.py -u 0.42159 -i 0.7638915 -o q3/highinstall-$i.user
./gUser.py -u 0.18629183 -i 0.08623483 -o q3/lowchange-$i.user
./gUser.py -u 0.5610895 -i 0.1918915 -o q3/highupdate-$i.user
./gUser.py -u 0.22202257 -i 0.25948714 -o q3/mediumchange-$i.user
done





