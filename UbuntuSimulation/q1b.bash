#!/bin/bash

#What effects does the update fequency have on the system
#5 users at .1 incrememnts

#create update and install
for i in `seq .1 .1 .9`
do for x in `seq 1 5`
do ./gUser.py -u $i -o q1b/u$i.$x.user
done
done

