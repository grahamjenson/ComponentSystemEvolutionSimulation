#!/bin/bash

#What effects does the install fequency have on the system
#30 users at .1 incrememnts

#create update and install
for i in 0.03 0.06 0.14 0.29
do for x in `seq 1 30`
do ./gUser.py -i $i -o q1c/i$i.$x.user
done
done
 

