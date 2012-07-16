#!/usr/bin/python
import os
from cudfpy import cudfpkg
import numpy
import time,datetime

import pylab

start = 1256814000 #one day before first action
end = 1288410600

initsys = cudfpkg.createProfileChangeRequest("9.10.cudf",nameversiononly=True)
startdate = datetime.datetime.fromtimestamp(1256900400)

def topnpairs(cudfmap):
	return set([(n,v) for n in cudfmap.keys() for v in cudfmap[n]])
	
	
def processSolutionsFolder(folder):
	assert os.path.isdir(folder)
	pcrl = {}
	for f in sorted(os.listdir(folder)):
		t = int(f.split(".")[0])
		
		ipcr = cudfpkg.createProfileChangeRequest(os.path.join(folder,f),nameversiononly=True)
		print folder,f,t
		pcrl[t] = ipcr
	return pcrl
	
def processOutputFile(ofile):
	of = open(ofile).readlines()
	t = -1
	ups = {}
	group = []
	for line in of: 
		line = line.strip()
		if line == "":
			#process Group
			if len(group) > 1:
				ts = int(group[0][17:27])
				for l in group:
					if l.startswith("*grade:"):
						l = l[8:-1].strip()
						ps = l.split(",")
						ps = map(str.strip,ps)
						if ps == [""]:
							ps = []
						ups[ts] = ps
						
			group = [] #ends
		else:
			group.append(line)
			
	
	upgrades = []
	for d in range(0,365):
		t = int(time.mktime((startdate + datetime.timedelta(days=d)).timetuple()))
		if t in sorted(ups.keys()):
			upgrades.append(ups[t])
		else:
			upgrades.append([])
	return upgrades
	

def countallrepeats(ofile):
	up = processOutputFile(ofile)
	
	daystill = []
	xy = 0
	for i in range(len(up)):
		for p in up[i]:
			#find the next upgrade
			for diff in range(i+1,len(up)):
				if p in up[diff]:
					daystill.append(diff-i)
					if diff-i <= 14:
						xy +=1
					break;

	
	
	arr = [0]*(max(daystill)+1)
	for i in daystill:
		arr[i] += 1
	dds = []
	
	for s in range(1,28):
		dds.append(sum(arr[:s]))
	
	return dds

def countrepeats(ofile,days):
	return countallrepeats(ofile)[days]


assert countrepeats("q1a/alwaysupdate.user.out",7) == 23


hir = []
hur = []
mcr = []
lcr = []
for i in range(1,51):
	hir.append(countallrepeats("q3/highinstall-%d.user.out" % i))
	lcr.append(countallrepeats("q3/lowchange-%d.user.out" % i))
	hur.append(countallrepeats("q3/highupdate-%d.user.out" % i))
	mcr.append(countallrepeats("q3/mediumchange-%d.user.out" % i))

mhir = numpy.mean(hir,axis=0)
mhur = numpy.mean(hur,axis=0)
mmcr = numpy.mean(mcr,axis=0)
mlcr = numpy.mean(lcr,axis=0)


pylab.plot(mhir,label="HI")
pylab.plot(mhur,label="HU")
pylab.plot(mmcr,label="MC")
pylab.plot(mlcr,label="LC")
pylab.plot(countallrepeats("q1a/alwaysupdate.user.out"),label="AU")
pylab.xlabel("Days")
pylab.ylabel("")

pylab.title("Unstable Package Installs for Users")

pylab.legend(loc="upper left")

#this shows that a user who installs frequently 
pylab.show()

	
