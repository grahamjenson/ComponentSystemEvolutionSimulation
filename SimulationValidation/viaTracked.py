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

totalcudfs = sorted(filter(lambda x: x.endswith(".cudfsystem"), os.listdir(totaltracked)))

simcudfs = map(lambda x : cudfpkg.createProfileChangeRequest(os.path.join(userfolder,x)),simcudfs)
trackedcudfs = map(lambda x : cudfpkg.createProfileChangeRequest(os.path.join(trackeddir,x)),trackedcudfs)

totalcudfs = map(lambda x : cudfpkg.createProfileChangeRequest(os.path.join(totaltracked,x)),totalcudfs)

def changed(cudfs):
	deltas = zip(cudfs[:-1],cudfs[1:])
	def new((prevcudf,newcudf)):
		updated = 0
		pnv = set(map(lambda x : (x.name,x.version) , prevcudf.getInstalled()))
		nnv = set(map(lambda x : (x.name,x.version) , newcudf.getInstalled()))
		
		return len(nnv - pnv)
	
	return map(new,deltas)
	
simupdates = changed(simcudfs)
trackedupdates = changed(trackedcudfs)
totalupdates = changed(totalcudfs)

print simupdates
print trackedupdates
print totalupdates

pylab.figure(1)
pylab.hist(simupdates)


pylab.figure(2)
pylab.hist(trackedupdates)

pylab.figure(3)
pylab.hist(totalupdates)

pylab.show()

