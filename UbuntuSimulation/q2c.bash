#!/bin/bash

#install everything at the same time, what are the differences, show time matters


#create neither
for u in q1a/i*.user;
do
fn=(`basename $u`) 
echo "9.10.cudf ; 1288868400.0" >> q2c/$fn
echo -n "1288410000;	install: "  >> q2c/$fn
tail -n+2 $u | awk '{print $3}' | tr -d "\n" | sed  "s/;/, /g" | sed  "s/, $//g">> q2c/$fn
echo ";	-removed,-changed,-uptodatedistance" >> q2c/$fn
done



