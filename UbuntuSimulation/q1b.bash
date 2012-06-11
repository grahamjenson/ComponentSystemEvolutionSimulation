#!/bin/bash

#What effects does the update fequency have on the system
#Once a month month = 12/365 = .05
#Twice a month = 24/365 = .1
#Once a week = .2
#Twice a week = .4

#create update and install
for i in 0.05 0.1 0.2 0.4
do for x in `seq 1 10`
do ./gUser.py -u $i -o q1b/u$i.$x.user
done
done

