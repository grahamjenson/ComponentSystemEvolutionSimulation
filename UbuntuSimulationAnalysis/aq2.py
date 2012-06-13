#!/usr/bin/python
import os
import pylab
from analysisutils import *
from cudfpy import cudfpkg

def q2a():
	def processTimes(userfile):
		lines =open(userfile+".out").readlines()
		lines = filter(lambda x: "time it took" in x, lines)
		lines = map(lambda x : x.split()[4].strip(),lines)
		ts = map(lambda x : float(x)/1000,lines)
		return ts
		
	pylab.figure(1)
	folder = "q1a"
	files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
	uinstalls = filter(lambda x : os.path.basename(x).startswith("u") and os.path.basename(x).endswith(".user") ,files);	
	installs = filter(lambda x : os.path.basename(x).startswith("i") and os.path.basename(x).endswith(".user") ,files);	
	always = os.path.join(folder,"alwaysupdate.user")
	
	times = []
	for f in installs:
		times.append(processTimes(f))
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(times)
	pylab.plot(range(len(uimean)),uimean,color='green',label="Install every day time to resolve")
	
	times = []
	for f in uinstalls:
		ts = processTimes(f)
		ts = zip(ts[0::2],ts[1::2])

		ts = map(lambda x : x[0] + x[1], ts)
		times.append(ts)

	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(times)
	pylab.plot(range(len(uimean)),uimean,color='red',label="Update and install total time to resolve")
	
	pylab.plot(range(len(uimean)),processTimes(always),color='blue',label="Update every day time to resolve")
	
	pylab.legend(loc="upper left")
	
	saveFigure("q2atime")
	#This graph shows that over time install and update and install requests to change the system take longer, likely becuase the problems are more complex consisting of more constraints.
	#These constraints are from the increasing number of components in the system, and that exist to be considered.
	
	
	
def q2b():
	pylab.figure(2)
	folder = "q1a"
	files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
	uinstalls = filter(lambda x : os.path.basename(x).startswith("u") and os.path.basename(x).endswith(".user") ,files);	
	installs = filter(lambda x : os.path.basename(x).startswith("i") and os.path.basename(x).endswith(".user") ,files);	
	
	def numberOfNotInstalled(userfile):
		ufolder = userfile +".sols"
		lastsol = os.path.join(ufolder,sorted(os.listdir(ufolder))[-1])
		cc = cudfpkg.createProfileChangeRequest(lastsol)
		lines = open(userfile).readlines()
		lines = filter(lambda x: "install:" in x, lines)
		times = map(lambda x : x.split()[0][:-1].strip(),lines)
		installs = map(lambda x : x.split()[2][:-1].strip(),lines)
		
		ns = []
		for t, i in zip(times,installs):
			if i not in cc.getPackageNames(onlyinstalled = True):
				ns.append((t,i))
		
		installedSucessfulRemovedLater = []
		notSatisfied = []
		for t,i in ns:
			isol = cudfpkg.createProfileChangeRequest(os.path.join(ufolder,str(t)+".cudfsystem"))
			if i in isol.getPackageNames(onlyinstalled = True):
				installedSucessfulRemovedLater.append(i)
			else:
				notSatisfied.append(i)
				
		return notSatisfied,installedSucessfulRemovedLater
	
	iii = []
	allns = {}
	for f in sorted(installs):
		ns,x = numberOfNotInstalled(f)
		iii.append(len(ns))
		for p in ns:
			if p not in allns:
				allns[p] = 0
			allns[p] = allns[p] +1
		
	print allns
		
	print iii
	#There are three reasons why a package that is requested to be installed does not get installed:
		#1: Its request for installation creates an unsatisfiable problem, so that it fails
		#2: It does not yet exist (packages randomly selected may not exist at the time they are attempted t be installed)
		#3: It takes too long to find a solution where it could be installed
	#These as these are independent of updating, they are identical between update and instal and install users,
	#[6, 6, 10, 8, 7, 7, 5, 8, 10, 9, 7, 7, 8, 10, 10, 7, 8, 8, 9, 7, 6, 9, 6, 9, 8, 5, 8, 12, 8, 9]
	# the most common packages that are in these categories are ('xul-ext-ubufox', 28), ('simple-scan', 25), ('quadrapassel', 21), ('kdelibs5-plugins', 20), ('chromium-browser', 15)
	
	#However, a possible thing is that a package installs, and that package is later removed.
	#For the install every day group 11 total users had unsatisfied install requests:
	#3 are the users that suffered soft failures. with 15,18,24 unsta requests
	#Of the remaining 8 users, 6 of such instances involved the package "quanta", and none of the 8 had more than 2 unsatisfied install requests.
	#This shows that other than through soft failure, or certain packages, this is unlikely to happen.

	
	#For the update and install group 13 users:
	#5 from soft failures, with 21,21,20,23,26 unsat install requests
	#4 of the remaining 8 include the package "quanta",
	#only one of the remaining 8 had more than 2 unsatisfied install requests
	
	
	
q2b()


pylab.show()
