#!/usr/bin/python
import numpy
import pylab
import datetime,time
import shelve
from numpy import arange

def s2d(i) : return i/(24*60*60)
def d2s(i) : return i*(24*60*60)

startdate = datetime.datetime(2009,10,31)

stratmap = [
		
		("Pro. APT w. Distance",	"Dist.APT.PRO",	'-uptodatedistance,-removed,-new'), #progressive
		("APT w. Distance",		"Dist.APT" ,	'-removed,-new,-uptodatedistance'),
		("Progressive Dist with Stable Constraints(28)", "Stable.Dist.Pro28",  '-stableversion(28),-uptodatedistance,-removed,-new'),
		("Conservative Dist with Stable Constraints(28)", "Stable.Dist.Con28",  '-stableversion(28),-removed,-new,-uptodatedistance'),
		("Progressive Dist with Stable Constraints(7)", "Stable.Dist.Pro7", '-stableversion(7),-uptodatedistance,-removed,-new'),
		("Conservative Dist with Stable Constraints(7)", "Stable.Dist.Con7", '-stableversion(7),-removed,-new,-uptodatedistance'),
		("Progressive Dist with Stable Constraints(14)", "Stable.Dist.Pro14", '-stableversion(14),-uptodatedistance,-removed,-new'),
		("Conservative Dist with Stable Constraints(14)", "Stable.Dist.Con14", '-stableversion(14),-removed,-new,-uptodatedistance')
	]
	
	
summary = {}
for name,strat,crit in stratmap:
	summary[strat] = shelve.open("." + strat + ".shelve")
	ham = summary[strat]["user-00-01-00"]["ham"]
	comps = summary[strat]["user-00-01-00"]["components"]
	print "%20s , %.2f , %.2f, %.2f %.2f %.2f" % (strat, len(ham), sum(ham), numpy.std(ham),len(comps),1.0 * sum(ham)/len(comps))

releases = [datetime.datetime(2009,4,23),datetime.datetime(2009,10,29),datetime.datetime(2010,4,29),datetime.datetime(2010,10,10)]
r = map(lambda x : time.mktime(x.timetuple()) , releases)

comps = {}
lines = open("allpacks").readlines()
for p,d in zip(lines[::3],lines[1::3]):
	comp = p.split()[1].strip()
	date = int(d.split()[1])
	if comp not in comps:
		comps[comp] = []
	comps[comp].append(date)




pylab.figure(1)
counter = -1
comps = comps.items()

#Filter the versions to a start and end date

bcomps = dict(comps)

print len(bcomps)

start = r[1]
end = r[3]
bcomps = map(lambda x: (x[0], filter(lambda v : v > start and v < end, x[1]) ),comps)

print len(bcomps)
#remove ones with low amount of versions
bcomps = filter(lambda x : len(x[1]) >= 5,bcomps)

print len(bcomps)
bcomps = filter(lambda x : not x[0].startswith("language-pack"), bcomps)
print len(bcomps)
bcomps = sorted(bcomps, key = lambda x : x[0])
print len(bcomps)
names = []
#for c,v in bcomps:
#	counter += 1
#	names.append(c)
#	for version in v:
#		pylab.scatter((version-start)/(60.*60*24),counter)
#pylab.yticks(range(counter),names)



#calculate the differences


alldiffs = []
for c,v in comps:
	#only releases of the simulation
	versions = map(datetime.datetime.fromtimestamp,v)
	versions = filter(lambda x : x > releases[0] and x < releases[-1], versions)
	#versions = filter(lambda x : x > (releases[2] - datetime.timedelta(days=30)) and x < (releases[2] + datetime.timedelta(days=30)), versions)
	diffs = map(lambda x : (x[1] - x[0]).days, zip(sorted(versions[:-1]),sorted(versions[1:])))
	alldiffs += diffs
	
pylab.figure(2)
aggdays = 1
alldiffs = filter(lambda x : x > 0 and x < 365, alldiffs)
pylab.hist(alldiffs,max(alldiffs)/aggdays)
pylab.xticks(arange(0,max(alldiffs),aggdays))

pylab.figure(3)
pylab.title("Days between component version releases (31/10/2009 - 31/10/2010)")
pylab.xlabel("Days between releases")
pylab.ylabel("Number of Version releases")
aggdays = 14
alldiffs = filter(lambda x : x > 0 and x < 365, alldiffs)
pylab.hist(alldiffs,max(alldiffs)/aggdays)
pylab.xticks(arange(0,max(alldiffs),aggdays*2))


pylab.figure(4)
aggdays = 28
alldiffs = filter(lambda x : x > 0 and x < 365, alldiffs)
pylab.hist(alldiffs,max(alldiffs)/aggdays)
pylab.xticks(arange(0,max(alldiffs),aggdays))


#pylab.figure(5)
#for name,strat,crit in stratmap:
#	user = "user-00-01-00" #daily
#	dist = summary[strat][user]["dist"]
#	pylab.plot(range(len(dist)),dist,label=name)
#pylab.legend(loc=2)
	
pylab.show()
