#!/usr/bin/python

#TODO Check other distributions to see if they fit better!!!


from scipy import stats
import sys
import tasks
from datetime import timedelta

import pylab
import sys
import numpy
import scipy
from scipy import fftpack
import os

files = filter(lambda x: x.endswith(".actionmap"),os.listdir("actionmaps"))
folder = "actionmaps"

x = 0
timeLength = 2
r = 60

for af in files :
		print af
		days, ntasks = tasks.createInstallTasks(os.path.join(folder,af), taskLength = timedelta(minutes=timeLength))
		dist = [0]*r
		
		for t in ntasks :
			dist[t] = dist[t] + 1
		print(af + " : " + str(max(ntasks)))
		tottasks = sum(dist)
		dist = map(lambda d: (1.0*d)/tottasks,dist)
		pylab.scatter(range(r),dist)
pylab.show()
