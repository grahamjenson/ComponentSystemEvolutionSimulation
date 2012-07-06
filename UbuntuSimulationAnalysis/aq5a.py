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


con1week = os.path.join(folder,"alwaysupdate.604800.user")
con2week = os.path.join(folder,"alwaysupdate.1209600.user")
con3week = os.path.join(folder,"alwaysupdate.1814400.user")
con4week = os.path.join(folder,"alwaysupdate.2419200.user")

always = os.path.join(folder,"alwaysupdate.user")

variables = [("Always Upgrade",always,"black"),("Upgrade with 1 week SV",con1week,"#FF0000"),("Upgrade with 2 weeks SV",con2week,"#00FF00"),("Upgrade with 3 weeks SV",con3week,"#0000FF"),
("Upgrade with 4 weeks SV",con4week,"#FF00FF")]


def plotuttdpc():
	pylab.figure(1)
	alluttd = numpy.array(uttdperc(always))
	
	for name,pf,c in variables:
		uttdpc = uttdperc(pf)
		print name, numpy.mean(uttdpc),uttdpc[-1]
		pylab.plot(pallthedays,uttdpc,label=("UTTDpC of 30 \"%s\" users" % name),color=c)

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Uptodate Distance per Component")
	pylab.title("Uptodate Distance per Component of Users")
	
	saveFigure("q5auttdperc")
	#This figure shows that a user who uses the stable version criteria will always be out of date when compared to a user that does not
	

	#The final value for each is
	#Conservative Upgrade mean 0.257869713007  final value 0.718897637795
	#Conservative Upgrade SV 1 week 0.283124650402 0.726771653543
	#Conservative Upgrade SV 2 week 0.308581816617 0.76062992126
	#Conservative Upgrade SV 3 week 0.336500150596 0.788976377953
	#Conservative Upgrade SV 4 week 0.361742179769 0.814960629921
	
	#so using the stable version metric set to 1 week, meant that it was on average .025 uttdpc less up to date.
	
def plotchange():
	fig = pylab.figure(10)
	
	allchange = chtt(always)
	diffdict = {}
	for name,pf,c in variables: 
		cht = chtt(pf)
		diffdict[name] = cht[-1]
		print name, numpy.mean(cht), cht[-1]-allchange[-1]
		pylab.plot(pallthedays,cht,label=name,color=c)	
	pylab.legend(loc="upper left")
	print "7 sum" , diffdict[variables[1][0]] - allchange[-7]
	print "14 sum", diffdict[variables[2][0]] - allchange[-14]
	print "21 sum", diffdict[variables[3][0]] - allchange[-21]
	print "28 sum", diffdict[variables[4][0]] - allchange[-28]
	
	saveFigure("q5achange")
	#This graph shows the total change of using a stable version criteria.
	#Conservative Upgrade mean 847.267759563 final value 1655.0
	#Conservative Upgrade SV 1 week 806.743169399 1622.0
	#Conservative Upgrade SV 2 week 774.00273224 1581.0
	#Conservative Upgrade SV 3 week 733.357923497 1536.0
	#Conservative Upgrade SV 4 week 699.601092896 1494.0
	
	#7 sum -28.0
	#14 sum -48.0
	#21 sum -58.0
	#28 sum -83.0

	#The difference of this change for the stable version once a week is that about 33.
	#To show that the stable user criteria is doing more than just delaying Upgrades by a week, and not effecting change, we can compare this reduction to the average weekly 
	#The average change in the final week was only 5 components. This means that over the year 33-5, 28 instances occured where a component was not unessesarily installed.
	
	#The average user had 26 changes in hte final 2 weeks, and the difference between the normal and 2 week SV criteria is 74.
	#This means that the SV 2 week criteria save 48 instances where a component was unessesarily installed.
	
	#for 3 weeks, 61 changes in the final week, 119.0 change diference, 58 unnessesary components not installed.
	#for 4 weeks, 78 changes in the final week, 161 change diference, 83 unnessesary components not installed.
	
	pylab.figure(11)
	for name,pf,c in variables[1:]: 
		cht = numpy.array(allchange) - numpy.array(chtt(pf))
		pylab.plot(pallthedays,cht,label=("Total Change of \"%s\" user" % name),color=c)
			
	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Total Change")
	pylab.title("Total Change of Users")
	
	saveFigure("q5adiffchange")
	#This shows that the difference becomes most notable during the april release.
	#This graph also shows a slight trend upwards for all criteria.
	#This is occuring as the criteria does not select.
	
#These simulations show that using the stable verison metric for a week decreased uttpc by about .025 but saved a total of 28 unnesseary component installations over a year.
#Both these numbers increase 
#These numbers are slight, though they would be expected to increase as system size increased.
#Lowering change, while minimially impacting the overall uptodateness of the component system was the stated goal of the Stable Version Criteira.
#he stable version critiera is deemed to be successful.

plotuttdpc()

plotchange()

