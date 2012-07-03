#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter
import datetime
from analysisutils import *


folder = "cache/q3"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
normalhighupdate = filter(lambda x : os.path.basename(x).startswith("highupdate"),files);
normalhighinstall = filter(lambda x : os.path.basename(x).startswith("highinstall"),files);
normallowchange = filter(lambda x : os.path.basename(x).startswith("lowchange"),files);
normalmediumchange = filter(lambda x : os.path.basename(x).startswith("mediumchange"),files);


folder = "cache/q6"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
conhighupdate = filter(lambda x : os.path.basename(x).startswith("conservativehighupdate"),files);
conhighinstall = filter(lambda x : os.path.basename(x).startswith("conservativehighinstall"),files);
conlowchange = filter(lambda x : os.path.basename(x).startswith("conservativelowchange"),files);
conmediumchange = filter(lambda x : os.path.basename(x).startswith("conservativemediumchange"),files);

prohighupdate = filter(lambda x : os.path.basename(x).startswith("progressivehighupdate"),files);
prohighinstall = filter(lambda x : os.path.basename(x).startswith("progressivehighinstall"),files);
prolowchange = filter(lambda x : os.path.basename(x).startswith("progressivelowchange"),files);
promediumchange = filter(lambda x : os.path.basename(x).startswith("progressivemediumchange"),files);


variables = [("High Install",normalhighinstall,"#00FF00"),("High Update",normalhighupdate,"#FF0000"),("Medium Change",normalmediumchange,"#FF00FF"),("Low Change",normallowchange,"#0000FF")]

convariables = [("Conservative High Install",conhighinstall,"#00FF55"),("Conservative High Update",conhighupdate,"#FF5500"),("Conservative Medium Change",conmediumchange,"#FF00AA"),("Conservative Low Change",conlowchange,"#5500FF")]

provariables = [("Progressive High Install",prohighinstall,"#00FFAA"),("Progressive High Update",prohighupdate,"#FFAA00"),("Progressive Medium Change",promediumchange,"#FF0055"),("Progressive Low Change",prolowchange,"#AA00FF")]

	
def plotcomputtdpc():
	pylab.figure(1)
	
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

	
	saveFigure("q6NormalUserComparisonuttd")
	
	
	
def plotcompchange():
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

	saveFigure("q6NormalUserComparisonChange")

def createuttdcompgraph(fig,name,useri):
	fig = pylab.figure(fig)
	for name,pf,c in [variables[useri],convariables[useri],provariables[useri]]: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.3)

	for name,pf,c in [variables[useri],convariables[useri],provariables[useri]]:
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean uttdpc",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=(name +"+-1std "))

	pylab.legend(loc="upper left")

	saveFigure(name)

def plotuttdProConNormal():
	createuttdcompgraph(20,"q6HighUpdateComparisonuttd",0);
	createuttdcompgraph(21,"q6HighUpdateComparisonuttd",1);
	createuttdcompgraph(22,"q6HighUpdateComparisonuttd",2);
	createuttdcompgraph(23,"q6HighUpdateComparisonuttd",3);
	

#plotcomputtdpc()

#plotcompchange()

plotuttdProConNormal()

pylab.show()
