#!/usr/bin/python
from creategraphs import parseActionMap

import sys
import numpy
import scipy
from scipy import fftpack
import os

files = filter(lambda x: x.endswith(".actionmap"),os.listdir("actionmaps"))

# 0 = install, 1 remove, 2 update
cor = numpy.zeros((3,3))
for f in files :
	days, ninst , cinst, nrem, crem, nup,cup = parseActionMap(open(f,'r'))
	for d in days :
		if ninst[d] > 0 :
			cor[0,0] = cor[0,0] + 1
			if nrem[d] > 0	:
				cor[0,1] = cor[0,1] + 1
			if nup[d] > 0 :	
				cor[0,2] = cor[0,2] + 1
		if nrem[d] > 0	:
			cor[1,1] = cor[1,1] + 1
			if ninst[d] > 0 :
				cor[1,0] = cor[1,0] + 1				
			if nup[d] > 0 :	
				cor[1,2] = cor[1,2] + 1
		if nup[d] > 0 :
			cor[2,2] = cor[2,2] + 1
			if ninst[d] > 0 :			
				cor[2,0] = cor[2,0] + 1
			if nrem[d] > 0	:
				cor[2,1] = cor[2,1] + 1

print cor
