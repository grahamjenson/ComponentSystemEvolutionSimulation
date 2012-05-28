#!/bin/bash

#What effects do extremem users have

#Have created users that 
#Always update
#Always install (if this is the case the randomness requires multiple users #to be generated)
#Always install and update 

#The always install and "always intsall and update" have identical pacakges #selected to be comparable



#create neither
./gUser.py -o q1a/never.user
#create update
./gUser.py -u 1 -o q1a/alwaysupdate.user
#create update and install
for i in `seq 1 30`; do ./gUser.py -i 1 -u 1 -o q1a/uandi$i.user; done

#Create install
for i in `seq 1 30`; do cat q1a/uandi$i.user | grep -v upgrade > q1a/i$i.user; done

for u in q1a/*.user; do echo mkdir $u"sols";./gjsolver $u $u"sols" 1> $u.out 2> $u.err >> ; done


