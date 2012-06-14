#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter
import datetime
from analysisutils import *


folder = "cache/q5a"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))


con1week = os.path.join(folder,"modalwaysupdate.604800.user")
con2week = os.path.join(folder,"modalwaysupdate.1209600.user")
con3week = os.path.join(folder,"modalwaysupdate.1814400.user")
con4week = os.path.join(folder,"modalwaysupdate.2419200.user")

always = os.path.join(folder,"alwaysupdate.user")

variables = [("Progressive Update",always,"black"),("Progressive Update SV 1 week",con1week,"#FF0000"),("Progressive Update SV 2 week",con2week,"#00FF00"),("Progressive Update SV 3 week",con3week,"#0000FF"),
("Progressive Update SV 4 week",con4week,"#FF00FF")]


	
def plotuttdpc():
	pylab.figure(1)
	alluttd = numpy.array(uttdperc(always))
	
	for name,pf,c in variables:
		uttdpc = uttdperc(pf)
		print name, numpy.mean(uttdpc),uttdpc[-1]
		pylab.plot(pallthedays,uttdpc,label=name,color=c)

	pylab.legend(loc="upper left")

	
	saveFigure("q5buttdperc")
	
	
	
def plotchange():
	fig = pylab.figure(10)
	
	allchange = chtt(always)
	
	for name,pf,c in variables: 
		cht = chtt(pf)
		print name, numpy.mean(cht),cht[-1]
		pylab.plot(pallthedays,cht,label=name,color=c)	
	pylab.legend(loc="upper left")

	saveFigure("q5bchange")
	

	
plotuttdpc()

plotchange()

pylab.show()
