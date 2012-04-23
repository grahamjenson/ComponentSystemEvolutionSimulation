#!/usr/bin/python
#This is the overall process, not neccessarily executed all at once

import time
import datetime
import subprocess
import os
import pylab

from cudfpy import cudfpkg
from gUser import getCudf

trackedsystemStartDate = datetime.datetime.fromtimestamp(1320109230)

twoYears = datetime.timedelta(days=730)

t0date = trackedsystemStartDate - twoYears
t0 = time.mktime(t0date.timetuple())

trackedrepsdir = "trackedSystem/reps"
trackedreps = map(lambda x: os.path.join(trackedrepsdir,x), sorted(filter(lambda x: x.endswith(".cudf"), os.listdir(trackedrepsdir))))




def summary(cudfs):
	
	print "newNames =", cudfpkg.newNames(cudfs)
	print "removedNames =",  cudfpkg.removedNames(cudfs)
	print "removedPackages =",  cudfpkg.removedPackages(cudfs)
	print "newPackages =",  cudfpkg.newPackages(cudfs)
	print "updatedPackages =",  cudfpkg.updatedPackages(cudfs)

	
if False:
	trackedcudfs = []
	for c in trackedreps:
		print c
		done = cudfpkg.createProfileChangeRequest(c,nameversiononly=True)
		trackedcudfs.append(done.toMap())
	


	summary(trackedcudfs)


repsdir = "reps"
repscudfs = []
for i in range(1,33):
	days = datetime.timedelta(days=i)
	t = time.mktime((t0date + days).timetuple())
	cudf = getCudf(t, repsdir)
	print cudf
	done = cudfpkg.createProfileChangeRequest(cudf,nameversiononly=True)
	repscudfs.append(done.toMap())
	
summary(repscudfs)

