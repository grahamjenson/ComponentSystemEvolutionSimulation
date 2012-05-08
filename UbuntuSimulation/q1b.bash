#!/bin/bash

#What effects does the update fequency have on the system
#5 users at .1 incrememnts

#create update and install
for i in `seq .1 .1 .9`
do for x in `seq 1 5`
do ./gUser.py -u $i -o q1b/u$i.$x.user
done
done
 
for u in q1b/*.user; do echo $u >> q1b.simlog;mkdir $u"sols";./gjsolver $u $u"sols" 1> $u.out 2> $u.err; done


