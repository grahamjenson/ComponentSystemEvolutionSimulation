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

modp0 = filter(lambda x : os.path.basename(x).startswith("modu0.03"),files);
modp1 = filter(lambda x : os.path.basename(x).startswith("modu0.06"),files);
modp2 = filter(lambda x : os.path.basename(x).startswith("modu0.14"),files);
modp4 = filter(lambda x : os.path.basename(x).startswith("modu0.29"),files);



variables = [] #[("Progressive Upgrade twice a month",modp1,"#0000FF"),("Progressive Upgrade once a week",modp2,"#00EEFF"),("Progressive Upgrade twice a week",modp4,"#00FF00")]


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
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 30 \"%s\" users" % name))

	pylab.plot(pallthedays,auttdpc,color='black',label="UTTDpC of \"Always Upgrade\" user")
	pylab.plot(pallthedays,modauttdpc,color='red',label="UTTDpC of \"Progressive Always Upgrade\" user")
	
	print "Last uttd always",auttdpc[-1]
	print "Last uttd mod always",modauttdpc[-1]
	
	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Uptodate Distance per Component")
	pylab.title("Uptodate Distance per Component of Users")
	pylab.ylim([0,1])
	saveFigure("q4auttdperc")
	
	#As can be seen from this figure the updating with the modified more prorgessive criteria allows the component system to stay more up to date.
	#At the end of the year the uttdpc for the conservative criteria is .718897637795, and for the more progressive criteria it is 0.481617647059.
	#This means that of a system of 1000 components, over 700 would have one better version available in the conservative users system, 
	#where under 500 would have a better version in the pprogressive users system.
	#An improvement of about 67%.
	#
	#It can also be seen that the release of the new system has less impact with the new criteria.
	#This is most evident with users who upgrade only twice a month, where before the release it their systems are on average more out of date than the alawys Upgrade user.
	#However, after the release the Upgrade once twice a month users system quickly becomes more up to date than the Upgrade every day conservative user.



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
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 30 \"%s\" users" % name))
	
	pylab.plot(pallthedays,chtalw, color="black", label="Total Change of \"Always Upgrade\" user")
	pylab.plot(pallthedays,chtmodalw, color="red", label="Total Change of \"Progressive Always Upgrade\" user")
	
	print "Last change always",chtalw[-1]
	print "Last change mod always",chtmodalw[-1]
	
	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Total Change")
	pylab.title("Total Change of Users")
	
	saveFigure("q4achange")
	#These are promising results, but how does this criteria efect the change of the system
	#the total change of the always Upgrade conservative user is 1655 component names over the year, where the progressive user is 2003.
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
	pylab.plot(pallthedays,nntal, color="black", label="Always Upgrade Mean change")
	pylab.plot(pallthedays,nntmod, color="red", label="Always Upgrade Mean change")
	
	print "Last new always",nntal[-1]
	print "Last new mod always",nntmod[-1]
	
	pylab.legend(loc="upper left")

	saveFigure("q4anew")
	#This figure first shows that the consertavive user never installs a new component.
	#Where the the progressive users install about 90 new components over the year.
	
	#So the conclusion of this experiment is that if an ubuntu system was able to be Upgraded in a progressive manner, 
	#the system would be 66% more up to date when compared to an identical conservative user, but would require 90 more components to be installed over the year.
	 

	
plotuttdpc()

plotchange()

plotnew()

