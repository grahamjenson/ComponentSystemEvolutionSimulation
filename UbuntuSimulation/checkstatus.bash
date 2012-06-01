#!/bin/bash

q1ad=`ls q1a | grep "err$" | wc | awk '{print $2}'`
q1au=`ls q1a | grep "user$" | wc | awk '{print $2}'`

q1bd=`ls q1b | grep "err$" | wc | awk '{print $2}'`
q1bu=`ls q1b | grep "user$" | wc | awk '{print $2}'`

q1cd=`ls q1c | grep "err$" | wc | awk '{print $2}'`
q1cu=`ls q1c | grep "user$" | wc | awk '{print $2}'`

q1ad=`ls q1a | grep "err$" | wc | awk '{print $2}'`
q1au=`ls q1a | grep "user$" | wc | awk '{print $2}'`

q1dd=`ls q1d | grep "err$" | wc | awk '{print $2}'`
q1du=`ls q1d | grep "user$" | wc | awk '{print $2}'`

q2cd=`ls q2c | grep "err$" | wc | awk '{print $2}'`
q2cu=`ls q2c | grep "user$" | wc | awk '{print $2}'`

q3d=`ls q3 | grep "err$" | wc | awk '{print $2}'`
q3u=`ls q3 | grep "user$" | wc | awk '{print $2}'`

q4ad=`ls q4a | grep "err$" | wc | awk '{print $2}'`
q4au=`ls q4a | grep "user$" | wc | awk '{print $2}'`

q5ad=`ls q5a | grep "err$" | wc | awk '{print $2}'`
q5au=`ls q5a | grep "user$" | wc | awk '{print $2}'`

q5bd=`ls q5b | grep "err$" | wc | awk '{print $2}'`
q5bu=`ls q5b | grep "user$" | wc | awk '{print $2}'`

echo q1a $q1ad $q1au
echo q1b $q1bd $q1bu
echo q1c $q1cd $q1cu
echo q1d $q1dd $q1du
echo q2c $q2cd $q2cu
echo q3 $q3d $q3u
echo q4a $q4ad $q4au
echo q5a $q5ad $q5au
echo q5b $q5bd $q5bu
