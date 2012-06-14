#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter
import datetime
from analysisutils import *


folder = "cache/q4a"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))

modp0 = filter(lambda x : os.path.basename(x).startswith("modu0.05"),files);
modp1 = filter(lambda x : os.path.basename(x).startswith("modu0.1"),files);
modp2 = filter(lambda x : os.path.basename(x).startswith("modu0.2"),files);
modp4 = filter(lambda x : os.path.basename(x).startswith("modu0.4"),files);



variables = [("Update twice a month",modp1,"#0000FF"),("Update once a week",modp2,"#00EEFF"),("Update twice a week",modp4,"#00FF00")]


always = os.path.join(folder,"alwaysupdate.user")
modalways = os.path.join(folder,"modalways.user")

	
def plotuttdpc():
	pylab.figure(1)

	auttdpc = uttdperc(always)
	modauttdpc = uttdperc(modalways)
	
	for name,pf,c in variables: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.3)
	
	for name,pf,c in variables: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		pylab.plot(pallthedays,imean,color=c,label=(name +"+-1std "))

	pylab.plot(pallthedays,auttdpc,color='black',label="Always Update Conservative uttdpc")
	pylab.plot(pallthedays,modauttdpc,color='red',label="Always Update Moderate uttdpc")
	
	print "Last uttd always",auttdpc[-1]
	print "Last uttd mod always",modauttdpc[-1]
	
	pylab.legend(loc="upper left")

	
	saveFigure("q4auttdperc")
	
	#As can be seen from this figure the updating with the modified more prorgessive criteria allows the component system to stay more up to date.
	#At the end of the year the uttdpc for the conservative criteria is .718897637795, and for the more progressive criteria it is 0.481617647059.
	#This means that of a system of 1000 components, over 700 would have one better version available in the conservative users system, 
	#where under 500 would have a better version in the pprogressive users system.
	#An improvement of about 67%.
	#
	#It can also be seen that the release of the new system has less impact with the new criteria.
	#This is most evident with users who upgrade only twice a month, where before the release it their systems are on average more out of date than the alawys update user.
	#However, after the release the update once twice a month users system quickly becomes more up to date than the update every day conservative user.



def plotchange():
	fig = pylab.figure(10)
	
	
	chtalw = chtt(always)
	chtmodalw= chtt(modalways)
	for name,pf,c in variables: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.3)
	
	for name,pf,c in variables: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		pylab.plot(pallthedays,imean,color=c,label=(name +"+-1std "))
	
	pylab.plot(pallthedays,chtalw, color="black", label="Always Update Mean change")
	pylab.plot(pallthedays,chtmodalw, color="red", label="Always Update Mean change")
	
	print "Last change always",chtalw[-1]
	print "Last change mod always",chtmodalw[-1]
	
	pylab.legend(loc="upper left")

	saveFigure("q4achange")
	#These are promising results, but how does this criteria efect the change of the system
	#the total change of the always update conservative user is 1655 component names over the year, where the progressive user is 2003.
	#This measn that over the year the progressive user changed just under 350 more components.
	#However, this change includes the additional updating of components, so the important metric is how many additional installed components were there.
	
def plotnew():
	fig = pylab.figure(20)
	
	
	
	for name,pf,c in variables: 
		ivals = map(lambda x : nntt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.3)
	
	for name,pf,c in variables: 
		ivals = map(lambda x : nntt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		pylab.plot(pallthedays,imean,color=c,label=(name +"+-1std "))
	
	nntal = nntt(always)
	nntmod =nntt(modalways) 
	pylab.plot(pallthedays,nntal, color="black", label="Always Update Mean change")
	pylab.plot(pallthedays,nntmod, color="red", label="Always Update Mean change")
	
	print "Last change always",nntal[-1]
	print "Last change mod always",nntmod[-1]
	
	pylab.legend(loc="upper left")

	saveFigure("q4anew")
	#This figure first shows that the consertavive user never installs a new component.
	#Where the the progressive users install about 90 new components over the year.
	
	#So the conclusion of this experiment is that if an ubuntu system was able to be updated in a progressive manner, 
	#the system would be 66% more up to date when compared to an identical conservative user, but would require 90 more components to be installed over the year.
	 

	
plotuttdpc()

plotchange()

plotnew()

pylab.show()
