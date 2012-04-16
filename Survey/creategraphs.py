#!/usr/bin/python

import pylab
import pickle
import sys
import scipy
import numpy
from matplotlib import dates
from scipy import fftpack
from pylab import array

def parseActionMap(actionfile):
	actionmap = pickle.load(actionfile)

	#task length if an action is within this length of time then we consider it the same task
	
	ordereddates = actionmap.keys()
	ordereddates.sort();

	datemap = {}
	firstday = int(round(dates.date2num(ordereddates[0])))
	for d in ordereddates :
		#normalize it such that days start at 0
		datemap[d] = int(round(dates.date2num(d)) - firstday)

	number_of_days = datemap[ordereddates[-1]] +1 #+1 to account for 0 being the first day
	days = range(number_of_days)

	#three actions, install upgrade and remove, with two types of information number and resulting change,
	#we are aggregating by days
	ninst = pylab.zeros(number_of_days)
	cinst =pylab.zeros(number_of_days)
	nrem = pylab.zeros(number_of_days)
	crem =pylab.zeros(number_of_days)
	nup = pylab.zeros(number_of_days)
	cup = pylab.zeros(number_of_days)

	for d in ordereddates :
		actions = actionmap[d]
		for a in actions :
			if a[0] == "install" :
				ninst[datemap[d]] = ninst[datemap[d]] + 1
				cinst[datemap[d]] = cinst[datemap[d]] + a[1]
			elif a[0] == "upgrade" :
				nup[datemap[d]] = nup[datemap[d]] + 1
				cup[datemap[d]] = cup[datemap[d]] + a[1]
			elif a[0] == "remove" :
				nrem[datemap[d]] = nrem[datemap[d]] + 1
				crem[datemap[d]] = crem[datemap[d]] + a[1]

	return (days, ninst,cinst,nrem,crem,nup,cup)

if __name__ == "__main__":
	actionfile = open(sys.argv[1],'r')
	days, ninst , cinst, nrem, crem, nup,cup = parseActionMap(actionfile)
	


	#Actions
	fig = 1
	pylab.figure(fig)
	fig = fig +1
		
	pylab.bar(days,ninst,color='b',label="install")
	#pylab.bar(days,nrem,color='r',bottom=ninst,label="remove")
	#pylab.bar(days,nup,color='g',bottom=[x+y for (x,y) in zip(ninst,nrem)],label="upgrade")

	pylab.legend()

	pylab.savefig(sys.argv[1] + ".actions"+ '.pdf',format="pdf")
	pylab.savefig(sys.argv[1] + ".actions"+ '.png',format="png")

	#Figures
	pylab.figure(fig)
	fig = fig +1

	pylab.bar(days,cinst,color='b',label="added")
	pylab.bar(days,crem,color='r',bottom=cinst,label="removed")
	pylab.bar(days,cup,color='g',bottom=[x+y for (x,y) in zip(cinst,crem)],label="upgraded")

	pylab.legend()

	pylab.savefig(sys.argv[1] + ".changes"+ '.pdf',format="pdf")
	#pylab.savefig(sys.argv[1] + ".changes"+ '.png',format="png")





