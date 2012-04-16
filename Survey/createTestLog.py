#!/usr/bin/python
from scipy.stats import poisson
from creategraphs import parseActionMap
import numpy
import os
import pylab
from random import random

files = os.listdir(".")
files = [f for f in files if f.endswith(".actionmap")]

rates = []
for f in files:
	days, ninst , cinst, nrem, crem, nup,cup = parseActionMap(open(f,'r'))
	vals = ninst
	
	mean = numpy.mean(ninst)
	std = numpy.std(ninst)
	
	vals = [v*(v < (mean+std*2)) for v in vals]
	rate = numpy.mean(vals)
	rates.append(rate)
	print f + " RATE " + str(rate)
rates.sort()
print rates
rate = numpy.median(rates)

stdrate = numpy.std(rates)

print "Mean Rate " + str(rate) + " STD Rate " + str(stdrate) 


def createPoissonModel(nodays,rate):
	vals = []
	for i in range(nodays-1):
		r = random()
		vals.append(poisson.ppf(r,rate))
	return vals


nodays = 1000

pylab.figure(1)
pylab.bar(range(nodays-1),createPoissonModel(nodays,rate))

pylab.figure(2)
pylab.bar(range(nodays-1),createPoissonModel(nodays,rate-stdrate))

pylab.figure(3)
pylab.bar(range(nodays-1),createPoissonModel(nodays,rate+stdrate))

pylab.show()
#array of ints of number of events



#for d in range(1,30) :
#	for f in range(0,(d % 4)*2) :
#		print "Start-Date: 2011-01-%.2d" % d + "  12:51:0"+str(f+1)
#		print "Install: x"
#		print "End-Date: 2011-01-02  12:51:09"
#		print 
