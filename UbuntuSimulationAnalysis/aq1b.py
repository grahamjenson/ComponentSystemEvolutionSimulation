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
p1 = filter(lambda x : os.path.basename(x).startswith("u0.1"),files);
p2 = filter(lambda x : os.path.basename(x).startswith("u0.2"),files);

p4 = filter(lambda x : os.path.basename(x).startswith("u0.4"),files);


always = os.path.join(folder,"alwaysupdate.user")
never = os.path.join(folder,"never.user")

variables = [("Update twice a month",p1,"red"),("Update once a week",p2,"pink"),("Update twice a week",p4,"blue")]

	
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
		pylab.plot(pallthedays,imean,color=c,label=(name +"+-1std "))
			        
	#for i in uinstalls:
	#	das,iuttdpd = uttdperc(i)
	#	pylab.plot(das,iuttdpd,color='black')

	pylab.plot(pallthedays,auttdpc,color='black',label="Always Update uttdpc")

	#pylab.plot(pallthedays,nuttdpc,color='r',label="Never Update uttdpc")	

	pylab.legend(loc="upper left")

	print "Last Always update uttdperc", auttdpc[-1]
	print "Last Never update uttdpperc", nuttdpc[-1]

	saveFigure("q1buttdperc")
	
	
plotuttdpc()
pylab.show()
