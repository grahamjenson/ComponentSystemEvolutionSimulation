#!/usr/bin/python

import pylab
import datetime,time
import shelve
from numpy import arange
import numpy


summary = {}
for strat in ["ntd.APTl","Dist.APT.PROl"]:
	summary[strat] = shelve.open("." + strat + ".shelve")

updatecycle = [62, 58, 54, 50, 46, 42, 40, 38, 36, 34, 32, 30, 28, 26, 24, 22, 20, 18, 16, 14, 12, 10, 8, 7 , 6 , 5, 4, 3, 2, 1, 0]
updatecycle = zip(range(len(updatecycle)),updatecycle)



for n,c in updatecycle:
	if c == 0:
		continue
	pylab.scatter(summary["ntd.APTl"]["user-00-%02d-00" % n]["hampd"],summary["ntd.APTl"]["user-00-%02d-00" % n]["distmean"], color = 'r')
	pylab.text(summary["ntd.APTl"]["user-00-%02d-00" % n]["hampd"],summary["ntd.APTl"]["user-00-%02d-00" % n]["distmean"], str(c))

for n,c in updatecycle:
	if c == 0:
		continue
	pylab.scatter(summary["Dist.APT.PROl"]["user-00-%02d-00" % n]["hampd"],summary["Dist.APT.PROl"]["user-00-%02d-00" % n]["distmean"], color = 'k')
	pylab.text(summary["Dist.APT.PROl"]["user-00-%02d-00" % n]["hampd"],summary["Dist.APT.PROl"]["user-00-%02d-00" % n]["distmean"], str(c))
	
pylab.show()
