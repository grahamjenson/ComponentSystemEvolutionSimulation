#!/usr/bin/python
import pylab
import numpy
from analysisutils import *


usersprobs = [("res1.log.actionmap" , 0.264151 , 0.358491 ),
("res6.log.actionmap" , 0.286885 , 0.204918 ),
("21.log.actionmap" , 0.315353 , 0.298755 ),
("res2.log.actionmap" , 0.219858 , 0.106383 ),
("28.log.actionmap" , 0.210191 , 0.254777 ),
("57.log.actionmap" , 0.115942 , 0.173913 ),
("23.log.actionmap" , 0.500000 , 0.136364 ),
("38.log.actionmap" , 0.709091 , 0.236364 ),
("56.log.actionmap" , 0.394904 , 0.872611 ),
("res5.log.actionmap" , 0.167273 , 0.214545 ),
("55.log.actionmap" , 0.140187 , 0.00000 ),
("54.log.actionmap" , 0.160305 , 0.209924 ),
("res4.log.actionmap" , 0.593407 , 0.208791 ),
("14.log.actionmap" , 0.066667 , 0.016667 ),
("res3.log.actionmap" , 0.448276 , 0.655172 ),
("44.log.actionmap" , 0.150000 , 0.275000 ),
("13.log.actionmap" , 0.354167 , 0.104167 ),
("3.log.actionmap" , 0.220930 , 0.116279 ),
("49.log.actionmap" , 0.441860 , 0.186047 )]

exupdate = "38.log.actionmap"
exinstall = "56.log.actionmap"
other = ["res1.log.actionmap", "res6.log.actionmap", "21.log.actionmap", "res2.log.actionmap", "28.log.actionmap"]

folder = "cache/q3"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
u1 = filter(lambda x : os.path.basename(x).startswith("u1"),files);
u2 = filter(lambda x : os.path.basename(x).startswith("u2"),files);
u3 = filter(lambda x : os.path.basename(x).startswith("u3"),files);
u4 = filter(lambda x : os.path.basename(x).startswith("u4"),files);
u5 = filter(lambda x : os.path.basename(x).startswith("u5"),files);

exu = filter(lambda x : os.path.basename(x).startswith("exupdate"),files);
exi = filter(lambda x : os.path.basename(x).startswith("exinstall"),files);

variables = [("Extreme update user",exu,"red"),("Extreme install user",exi,"blue"),("User 1",u1,"#FFFF00"),("User 2",u2,"#FF00FF"),("User 3",u3,"#00FFFF"),("User 4",u4,"#FF11FF"),("User 5",u5,"#FFAAFF")]
def euser():
	pylab.figure(1)
	for n,u,i in usersprobs:
		if i == 0.0 or u == 0.0: continue
		if n == exupdate or n==exinstall or n in other:
			pylab.scatter(u,i,color="red")
		else:
			pylab.scatter(u,i,color="blue")
			
		saveFigure("q3users")


def plotuttdpc():
	pylab.figure(10)

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

	
	saveFigure("q3uttdperc")
	


def plotchange():
	fig = pylab.figure(20)
	
	
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

	saveFigure("q3change")
	
	
euser()
#plotuttdpc()
#plotchange()

pylab.show()
