#!/usr/bin/python

from sklearn.cluster import KMeans
import pylab
import numpy 
from analysisutils import *


def dist(x,y):   
    return numpy.sqrt(numpy.sum((x-y)**2))
    
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

kval = 4
colors = ["red","green","blue","cyan", "magenta"]*10
points = map(lambda x: (x[1],x[2]),usersprobs)

k = KMeans(k=kval)

k.fit(points)



for kv in range(kval):
	for i in range(len(points)):
		if k.labels_[i] == kv:
			pylab.scatter(points[i][0],points[i][1],color = colors[kv])
			
ps = 	[("High Install (HI)",0.42159,0.7638915),
	("Low Change (LC)",0.18629183,0.08623483),
	("High Update (HU)",0.5610895,0.1918915),
	("Medium Change (MC)",0.22202257,0.25948714)]

for n,x,y in ps:
	pylab.scatter(x,y,color="black",marker="x")
	pylab.text(x,y," " + n,verticalalignment="center")



#pylab.scatter(p[0],p[1],color="black",marker="x")

terr = []
for kv in range(kval):
	kerr = 0
	for i in range(len(points)):
		if k.labels_[i] == kv:
			err =  dist(numpy.array(points[i]),k.cluster_centers_[kv])
			kerr += err
	terr.append(kerr)
	
print numpy.mean(terr),terr

print k.cluster_centers_

pylab.xlabel("Update Probability")
pylab.ylabel("Install Probability")
pylab.ylim([0,1])
saveFigure("userlogAnalysis")
	
