#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter
import datetime
from analysisutils import *


folder = "cache/q1b"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
p0 = filter(lambda x : os.path.basename(x).startswith("u0.05"),files);
p1 = filter(lambda x : os.path.basename(x).startswith("u0.1"),files);
p2 = filter(lambda x : os.path.basename(x).startswith("u0.2"),files);
p4 = filter(lambda x : os.path.basename(x).startswith("u0.4"),files);

always = os.path.join(folder,"alwaysupdate.user")
never = os.path.join(folder,"never.user")

variables = [("Update once a month",p0,"#FF0000"),("Update twice a month",p1,"#0000FF"),("Update once a week",p2,"#00EEFF"),("Update twice a week",p4,"#00FF00")]

	
def plotuttdpc():
	pylab.figure(1)

	auttdpc = uttdperc(always)
	nuttdpc = uttdperc(never)
	
	for name,pf,c in variables: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.3)
	
	for name,pf,c in variables: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean uttdpc",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=(name +"+-1std "))

	pylab.plot(pallthedays,auttdpc,color='black',label="Always Update uttdpc")

	pylab.legend(loc="upper left")

	
	saveFigure("q1buttdperc")
	
	
	#The main point to note on the graph is that updating less frequently has the most significant effect when the components are evolving quickly.
	#In the months before the release the differences in "out of dateness" of the systems is proportional to how infrequently they are updated.
	#However, around the April release when components are being maintained more aggressivly, infrequently updated systems take significantly more time to adapt.
	
	#This is an important link between component evolution, and CSE: \textbf{quickly evolving components demands quickly evolving systems.}
	#This can be seen in the difference in uptodate distance  between the update everyday user and other users.
	
	#Mean difference to alwys updating Update once a month 0.061175078525
	#Mean difference to alwys updating Update twice a month 0.0320382083387
	#Mean difference to alwys updating Update once a week 0.0163829009079 
	#Mean difference to alwys updating Update twice a week 0.00503248569339
	
	#This data shows a possible corolation between the uptodateness of a system and 1 over the probability to update.
	
	#To test the hypothesis that the uptodate ness of a system is proportional to 1 over the probability to update we did further experiments
	pylab.figure(2)
	probs = {}
	for f in files:
		if f.endswith("never.user"):
			#probs[0.0] = [f]
			continue
		if f.endswith("alwaysupdate.user"): 
			probs[1.0] = [f]
			continue
		up = float("." + f.split(".")[1])
		if up not in probs:
			probs[up] = []
		probs[up].append(f)
	
	xy = []
	for up in probs.keys():
		pf = probs[up]
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		xy.append((1.0/up,numpy.mean(imean)))
		print "uttdpc", up, 1.0/up, numpy.mean(imean)
		#mdiff = numpy.mean(numpy.array(imean)-auttdpc)
	
	x,y = zip(*sorted(xy))
	pylab.plot(x,y)
	pylab.scatter(x,y)
	saveFigure("q1bcorrolationuttdpc")
	
	#The corrolcation between these two properties shows the dimishing returns on updating frequently.
	#For example, updating twice a week (with a update probability of 0.4) is on average 0.262902198701 out of date,
	#where updating twice as much (with an update probability of .8) is on average 0.258912267114 out of date, an insignificant amount.
	


def plotchange():
	fig = pylab.figure(10)
	
	pylab.plot(pallthedays,chtt(always), color="black", label="Always Update Mean change")
	
	for name,pf,c in variables: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.3)
	
	for name,pf,c in variables: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean change",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=(name +"+-1std "))
	
	pylab.legend(loc="upper left")

	saveFigure("q1bchange")

	#The main point that can be extracted from this graph is that by updating less frequently less change is required.
	#This is because a single component could potentially release multiple versions quickly which a user who updates every day would install each one,
	#where a user who installs once a month may skip over the interim versions and only install the latest version.
	#This phenonomon is discussed and used later to create a criterion that takes advantage and lower change.
	
		
	pylab.figure(11)
	probs = {}
	for f in files:
		if f.endswith("never.user"):
			#probs[0.0] = [f]
			continue
		if f.endswith("alwaysupdate.user"): 
			probs[1.0] = [f]
			continue
		up = float("." + f.split(".")[1])
		if up not in probs:
			probs[up] = []
		probs[up].append(f)
	
	xy = []
	for up in probs.keys():
		pf = probs[up]
		ivals = map(lambda x : chpd(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		xy.append((1.0/up,numpy.sum(imean)))
		print "change", up, 1.0/up, numpy.sum(imean)
		#mdiff = numpy.mean(numpy.array(imean)-auttdpc)
	
	x,y = zip(*sorted(xy))
	pylab.plot(x,y)
	pylab.scatter(x,y)
	saveFigure("q1bcorrolationchange")
	
	#The inverse corrolation between 1 over the update probability and the total change demonstrates the increasing returns of updating less frequently.
	#For example, updating twice a week (with a update probability of 0.4) changes on average a total of 1644.6 over a year,
	#where updating half as much (with an update probability of .2) is changes on average a total of 1627.7 a year.
	#This means that updating half as much means over a year changes about less than 17 components.




def pseudoSystem():

	#The corrolations discovered when answering this question shows the emergent effects of component evolution on CSE.
	#We can further study these correlations by creating an example component system consisting of many components where a a different component has a new version released every day.
	#By looking at users who update the system at different frequencies over 100 days, we present two graphs, update frequency to uttd and change.	
	
	
	l = 100
	uttdxy = []
	chxy = []
	for uf in range(1,20):
		uttd = range(uf)*(l*2/uf)
		uttd = uttd[1:l+1]
		uttdxy.append((uf,numpy.mean(uttd)))
		
		sh = [0]*(uf-1) + [uf]
		sh = sh*l
		sh = sh[:l]
		chxy.append((uf,sum(sh)))
		
	pylab.figure(20)
	x,y = zip(*sorted(uttdxy))
	pylab.plot(x,y)
	pylab.scatter(x,y)
	saveFigure("q1bpseudouttd")
	
	pylab.figure(21)
	
	x,y = zip(*sorted(chxy))
	pylab.plot(x,numpy.array(y))
	pylab.scatter(x,numpy.array(y))
	pylab.ylim([0,l+10])
	saveFigure("q1bpseudochange")
	
	#What we can see by creating this simple example is that the uttd correlation to update frequency is predicted here.
	#This shows that the system is getting outofdate at a constant rate. 
	#However, the more interesting point is that the correlation between update frequency and change is not present in this simple example.
	#This means that another varaible is present when updating a component system.
	
	#That variable is the rate at which the same component is updated multiple times.
	#In the simple example different components are updated regularly. 
	#However, in reality the same component can be upgraded multiple times, lowering the change necessary when updating.
	#This effect is further discussed and taken advantage of in later sections.
	
pseudoSystem()	
plotuttdpc()

plotchange()

pylab.show()
