#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter
import datetime
from analysisutils import *


folder = "cache/q1c"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
p0 = filter(lambda x : os.path.basename(x).startswith("i0.05"),files);
p1 = filter(lambda x : os.path.basename(x).startswith("i0.1"),files);
p2 = filter(lambda x : os.path.basename(x).startswith("i0.2"),files);
p3 = filter(lambda x : os.path.basename(x).startswith("i0.3"),files);
p4 = filter(lambda x : os.path.basename(x).startswith("i0.4"),files);
p5 = filter(lambda x : os.path.basename(x).startswith("i0.5"),files);
p6 = filter(lambda x : os.path.basename(x).startswith("i0.6"),files);
p7 = filter(lambda x : os.path.basename(x).startswith("i0.7"),files);
p8 = filter(lambda x : os.path.basename(x).startswith("i0.8"),files);
p9 = filter(lambda x : os.path.basename(x).startswith("i0.9"),files);

always = filter(lambda x : not os.path.basename(x).startswith("i0") and os.path.basename(x).startswith("i"),files);
never = os.path.join(folder,"never.user")

alll = always+p0+p1+p2+p3+p4+p5+p6+p7+p8+p9

variables = [("Install every day",always,"#FF00FF"),("Install once a month",p0,"#FF0000"),("Install twice a month",p1,"#0000FF"),("Install once a week",p2,"#00EEFF"),("Install twice a week",p4,"#00FF00")]

	
def plotuttdpc():
	pylab.figure(1)

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


	pylab.legend(loc="upper left")

	
	saveFigure("q1cuttdperc")
	#Viewing this graph reaffirms the previously stated hypothesis that the uttdperc metric is invariable to the probability a user installs components.
	


def plotchange():
	fig = pylab.figure(10)
	
	
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

	saveFigure("q1cchange")
	#This graph shows the range over which change can occur whn altering the probability to install.
	# given the results from this supports the idea that the probability to install corrolates to the amount of packages installed, this is straight forward.
	#An effect that did not occur, which was hypothesised might, was that over time the amount of installed packages decreases as common dependended packages are installed.
	#This does not occur, but may be because of the randomness that packages are selected.
	#It may still occur in reaility, as say a graphics designer will likely install graphics components that will require similar functionality.
	
	#Another interesting point from that can be seen in this graphi is the variability created by the soft failures, as discussed previously.
	# it can be seen that the std increased after a soft failure in install twice a week change curve.
	uivals = zip(alll,map(lambda x : rempd(x),alll))
	for name,ui in uivals:
		for date, remv in zip(allthedays,ui):
			if remv >= 100:
				print date,datetime.date.fromtimestamp(date),name
	#this soft failure was caused by
	# req=install: balazar, 1278154800 2010-07-03 cache/q1c/i0.4.29.user
	#Although not conclusive, it may be reasonable to assume that soft failures occur very rarely.
	#However, as with the above conclusion this may be an artifact of installing packages randomly, and may occur more or less frequently depending on the type of packages a user is likely to install.
	
plotuttdpc()
plotchange()
pylab.show()

