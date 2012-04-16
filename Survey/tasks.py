#!/usr/bin/python

import pickle
from datetime import datetime
from datetime import timedelta
from matplotlib import dates

def createInstallTasks(actionfile, taskLength = timedelta(minutes=120)):
	actionmap = pickle.load(open(actionfile,"r"))

	#task length if an action is within this length of time then we consider it the same task
		

	ordereddates = actionmap.keys()
	ordereddates.sort();

	datemap = {}
	firstday = int(round(dates.date2num(ordereddates[0])))
	for date in ordereddates :
		#normalize it such that days start at 0
		datemap[date] = int(round(dates.date2num(date)) - firstday)

	number_of_days = datemap[ordereddates[-1]]+1 #+1 to account for 0 being the first day
	days = range(number_of_days)

	#three actions, install upgrade and remove, with two types of information number and resulting change,
	#we are aggregating by days
	
	
	#date to task
	tasks = []
	initdate = ordereddates[0] 
	prevd = ordereddates[0]
	for d in ordereddates :
		installs = actionmap[d]["install"]
		if installs == 0 : continue
		
		if prevd+taskLength < d :
			tasks.append(datemap[initdate])
			initdate = d		
		#if it is outside the date
					
		prevd = d

	ntasks = [0]*number_of_days
	for t in tasks :
		ntasks[t] = ntasks[t] + 1

	return (days,ntasks)


