#!/usr/bin/python
#This is the overall process, not neccessarily executed all at once

import time
import datetime
import subprocess
import os
import pylab

from cudfpy import cudfpkg

trackedsystemStartDate = datetime.datetime.fromtimestamp(1320109230)

twoYears = datetime.timedelta(days=730)

t0date = trackedsystemStartDate - twoYears
t0 = time.mktime(t0date.timetuple())

userfile = "trackedsim.user"
#process = subprocess.Popen(["./gUser.py","-a","9.10.cudf","-t",str(int(t0)),"-T",str(31),"-u",str(1.0),"-U","\"-removed,-new,-uptodatedistance\"","-o",])
#process.wait()

userfolder = "trackedsim"
trackeddir = "trackedSystem"
totaltracked = "totalSimulatedUser"

#process = subprocess.Popen(["./runSimulation.py", "-u", userfile, "-o", userfolder])
#process.wait()

#./gUser.py -a trackedSystem/1320109230.installed.cudf -t 1320109320 -T 31 -u 1.0 -U="-removed,-new,-uptodatedistance" -o totalSimulatedUser.user -r trackedSystem/reps/

simcudfs = sorted(filter(lambda x: x.endswith(".cudfsystem"), os.listdir(userfolder)))
trackedcudfs = sorted(filter(lambda x: x.endswith(".installed.cudf"), os.listdir(trackeddir)))

simcudfs = map(lambda x : cudfpkg.createProfileChangeRequest(os.path.join(userfolder,x),nameversiononly=True).toMap(),simcudfs)
trackedcudfs = map(lambda x : cudfpkg.createProfileChangeRequest(os.path.join(trackeddir,x),nameversiononly=True).toMap(),trackedcudfs)




def summary(cudfs):
	print "newNames =", cudfpkg.newNames(cudfs)
	print "removedNames =",  cudfpkg.removedNames(cudfs)
	print "removedPackages =",  cudfpkg.removedPackages(cudfs)
	print "newPackages =",  cudfpkg.newPackages(cudfs)
	print "updatedPackages =",  cudfpkg.updatedPackages(cudfs)

print "Simulation"
summary(simcudfs)

print "REAL"

summary(trackedcudfs)
