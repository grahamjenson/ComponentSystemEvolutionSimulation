#!/bin/bash

#What effects does the update fequency have on the system
#5 users at .1 incrememnts

#create update and install1
./gUser.py -u 1 -U"-removed,-uptodatedistance,-new" -o q4a/modalways.user


for i in 0.03 0.06 0.14 0.29
do for x in `seq 1 10`
do ./gUser.py -u $i -U"-removed,-uptodatedistance,-new" -o q4a/modu$i.$x.user
done
done



