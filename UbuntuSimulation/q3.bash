#!/bin/bash

#Simulated Real Users
#Extreme users
#38.log.actionmap , 0.709091 , 0.236364 
#56.log.actionmap , 0.394904 , 0.872611

#not extreme users
#res1.log.actionmap , 0.264151 , 0.358491 
#res6.log.actionmap , 0.286885 , 0.204918 
#21.log.actionmap , 0.315353 , 0.298755 
#res2.log.actionmap , 0.219858 , 0.106383 
#28.log.actionmap , 0.210191 , 0.254777 



for i in `seq 1 30`; 
do 
./gUser.py -u 0.709091 -i 0.236364 -o q3/exupdate-$i.user
./gUser.py -u 0.394904 -i 0.872611 -o q3/exinstall-$i.user

./gUser.py -u 0.264151 -i 0.358491 -o q3/u1-$i.user
./gUser.py -u 0.286885 -i 0.204918 -o q3/u2-$i.user
./gUser.py -u 0.315353 -i 0.298755 -o q3/u3-$i.user
./gUser.py -u 0.219858 -i 0.106383 -o q3/u4-$i.user
./gUser.py -u 0.210191 -i 0.254777 -o q3/u5-$i.user

done





