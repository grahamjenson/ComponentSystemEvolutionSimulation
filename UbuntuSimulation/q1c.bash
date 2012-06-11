#!/bin/bash

#What effects does the install fequency have on the system
#30 users at .1 incrememnts

#create update and install
for i in 0.05 0.1 0.2 0.4
do for x in `seq 1 30`
do ./gUser.py -i $i -o q1c/i$i.$x.user
done
done
 

