#!/usr/bin/python
import os
import pickle 
import datetime
import tasks
import pylab

os.listdir("actionmaps")
x = map(lambda x : os.path.join("actionmaps",x),filter(lambda x : x.endswith("actionmap"),os.listdir("actionmaps")))

###First we proces what an update and a install is (an install is when a package is installed, an update only if a package is updated with no install)

installs = {}
updates = {}
for pf in x:
	installs[pf] = {}
	updates[pf] = {}
	am = pickle.load(open(pf))
	for da in am.keys():
		
		day = datetime.datetime(da.year,da.month,da.day)
		if day not in installs[pf].keys(): 
			installs[pf][day] = 0
			updates[pf][day] = 0
		if am[da]["install"] > 0:
			installs[pf][day] += 1
		if am[da]["upgrade"] > 0 and am[da]["install"] == 0:
			updates[pf][day] = 1

installranges = {}
updateranges = {}
for pf in installs.keys():
	mind = min(installs[pf].keys())
	maxd = max(installs[pf].keys())
	td = datetime.timedelta(days=1)
	
	installranges[pf] = []
	updateranges[pf] = []
	while mind < maxd:
		if mind in installs[pf].keys():
			installranges[pf].append(installs[pf][mind])
			updateranges[pf].append(1)
		else:
			installranges[pf].append(0)
			updateranges[pf].append(0)
		mind = mind + td
		
def todist(li):
	dist = {}
	for i in li:
		if i not in dist:
			dist[i] = 0
		dist[i] += 1
	#print sorted(dist.items(),key = lambda x: x[0])
	dd = []
	total = sum(dist.values())*1.0
	for i in range(max(dist.keys())+1):
		n = 0;
		if i in dist.keys():
			n = dist[i]/total
		else:
			n = 0
		dd.append(n)
	return dd	
			

dists = []
for pf in installranges:
	#print pf
	installrate = 1.0*sum(installranges[pf])/len(installranges[pf])
	updaterate = 1.0*sum(updateranges[pf])/len(updateranges[pf])
	#print "    ",installrate,len(installranges[pf])
	#print "    ", updaterate,len(updateranges[pf])
	dist = todist(installranges[pf])
	dists.append((updaterate,dist))
	pylab.scatter(installrate,updaterate)
	#print len(filter(lambda x: x,map(lambda x : (x[0] > 0 and x[1] > 0) or (x[0] == 0), zip(installranges[pf],updateranges[pf])))) , len(updateranges[pf])
	#print map(lambda x : (x[0] > 0 and x[1] > 0) or (x[0] == 0), zip(installranges[pf],updateranges[pf]))

dists = sorted(dists,key = lambda x : x[0])

print ""
print ""
print "\\begin{table}"
print "\\begin{tabular}{|l|l ||  p{8.5cm}|}"

lines = "\\hline User \\# & Update Probability & Install Distribution  \\\\ \\hline \\hline\n"
i = 1
for up,idist in dists:
	lines += str(i) + "  &  " + ("%0.2f" % up) + "\t & \t" 
	i+=1
	delim = ""
	for a in map(lambda x: "(%d : %0.1f)" % (x[0],x[1]*100), filter(lambda x: x[1] > 0.005 , enumerate(idist))):
		lines += delim + a
		delim = ", "
	lines += "\\\\ \\hline \n" 
	
print lines
print "\\end{tabular}"
print "\\caption[Extracted User Log Information]{The set of extracted update probabilities and user install distributions extracted from the submitted user logs."
print "The install distributions are shown as a set of pairs, where the first element is the number of packages, and the second is the probability of this amount being installed, "
print "e.g. (2 : 0.03) means the user has a 3\% chance of installing 2 packages on a day."
print "For presentations sake, pairs that have less than 0.005 probability are removed.}"
print "\\label{userlogvariables}"
print "\\end{table}"
print ""
print ""
#pylab.hist(updatedists,10)
#pylab.show()

