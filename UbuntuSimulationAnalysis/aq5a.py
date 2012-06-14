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

variables = [("Conservative Update",always,"black"),("Conservative Update SV 1 week",con1week,"#FF0000"),("Conservative Update SV 2 week",con2week,"#00FF00"),("Conservative Update SV 3 week",con3week,"#0000FF"),
("Conservative Update SV 4 week",con4week,"#FF00FF")]





	
def plotuttdpc():
	pylab.figure(1)
	alluttd = numpy.array(uttdperc(always))
	
	for name,pf,c in variables:
		uttdpc = uttdperc(pf)
		print name, numpy.mean(uttdpc),uttdpc[-1]
		pylab.plot(pallthedays,uttdpc,label=name,color=c)

	pylab.legend(loc="upper left")

	
	saveFigure("q5auttdperc")
	#This figure shows that a user who uses the stable version criteria will always be out of date when compared to a user that does not
	

	#The final value for each is
	#Conservative Update mean 0.257869713007  final value 0.718897637795
	#Conservative Update SV 1 week 0.283124650402 0.726771653543
	#Conservative Update SV 2 week 0.308581816617 0.76062992126
	#Conservative Update SV 3 week 0.336500150596 0.788976377953
	#Conservative Update SV 4 week 0.361742179769 0.814960629921
	
	#so using the stable version metric set to 1 week, meant that it was on average .025 uttdpc less up to date.
	
def plotchange():
	fig = pylab.figure(10)
	
	allchange = chtt(always)
	
	for name,pf,c in variables: 
		cht = chtt(pf)
		print name, numpy.mean(cht), cht[-1]-allchange[-1]
		pylab.plot(pallthedays,cht,label=name,color=c)	
	pylab.legend(loc="upper left")
	print "28 sum" ,allchange[-1] - allchange[-28]
	print "21 sum", allchange[-1] - allchange[-21]
	print "14 sum" ,allchange[-1] - allchange[-14]
	print "7 sum" ,allchange[-1] - allchange[-7]
	saveFigure("q5achange")
	#This graph shows the total change of using a stable version criteria.
	#Conservative Update mean 847.267759563 final value 1655.0
	#Conservative Update SV 1 week 806.743169399 1622.0
	#Conservative Update SV 2 week 774.00273224 1581.0
	#Conservative Update SV 3 week 733.357923497 1536.0
	#Conservative Update SV 4 week 699.601092896 1494.0
	
	
	#The difference of this change for the stable version once a week is that about 33.
	#To show that the stable user criteria is doing more than just delaying updates by a week, and not effecting change, we can compare this reduction to the average weekly 
	#The average change in the final week was only 5 components. This means that over the year 33-5, 28 instances occured where a component was not unessesarily installed.
	
	#The average user had 26 changes in hte final 2 weeks, and the difference between the normal and 2 week SV criteria is 74.
	#This means that the SV 2 week criteria save 48 instances where a component was unessesarily installed.
	
	#for 3 weeks, 61 changes in the final week, 119.0 change diference, 58 unnessesary components not installed.
	#for 4 weeks, 78 changes in the final week, 161 change diference, 83 unnessesary components not installed.
	
	pylab.figure(11)
	for name,pf,c in variables[1:]: 
		cht = numpy.array(allchange) - numpy.array(chtt(pf))
		pylab.plot(pallthedays,cht,label=name,color=c)	
	pylab.legend(loc="upper left")

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

pylab.show()
