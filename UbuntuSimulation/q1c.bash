#!/bin/bash

#What effects does the install fequency have on the system
#30 users at .1 incrememnts

#create update and install
for i in `seq .1 .1 .9`
do for x in `seq 1 30`
do ./gUser.py -i $i -o q1c/i$i.$x.user
done
done
 
for u in q1c/*.user; do echo $u >> q1c.simlog;mkdir $u"sols";./gjsolver $u $u"sols" 1> $u.out 2> $u.err; done

