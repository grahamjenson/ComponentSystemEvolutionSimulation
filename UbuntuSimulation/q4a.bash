#!/bin/bash

#What effects does the update fequency have on the system
#5 users at .1 incrememnts

#create update and install1
for i in `seq .1 .1 1`
do for x in `seq 1 5`
do ./gUser.py -u $i -U"-uptodatedistance,-removed,-new" -o q4a/maxu$i.$x.user
done
done

for i in `seq .1 .1 1`
do for x in `seq 1 5`
do ./gUser.py -u $i -U"-removed,-uptodatedistance,-new" -o q4a/modu$i.$x.user
done
done

for u in q4a/*.user; do echo $u >> q4a.simlog;mkdir $u"sols";./gjsolver $u $u"sols" 1> $u.out 2> $u.err; done


