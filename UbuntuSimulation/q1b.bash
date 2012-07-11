#!/bin/bash

#What effects does the update fequency have on the system
#Once a month month = 12/365 = .03
#Twice a month = 24/365 = .06
#Once a week = .14
#Twice a week = .29

#create update and install
for i in 0.03 0.06 0.14 0.29
do for x in `seq 1 10`
do ./gUser.py -u $i -o q1b/u$i.$x.user
done
done

