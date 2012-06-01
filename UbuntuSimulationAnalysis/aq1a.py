#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter

start = 1256814000 #one day before first action
end = 1288410600

day = (24*60*60)
week = day*7
def epoch2date(t):
	return dates.num2date(dates.epoch2num(t))

	
allthedays = range(start,end, day )
pallthedays = map(lambda da: epoch2date(da),allthedays)

alltheweeks = range(start,end, week)
palltheweeks = map(lambda da: epoch2date(da),alltheweeks)

def saveFigure(name):
	pylab.gcf().set_size_inches(15,9)
	pylab.savefig("plots/"+name,pad_inches=0.1,bbox_inches='tight')
	

	
#returns the value at time (e.g. system size) 
def getValueAtTime(t,values):
	for ds,v in sorted(values,key=lambda x: -x[0]):
		if ds <= t:
			return v
			
#Returns sum of values plus minus 12 hours of time (e.g. change)
def getOnDate(t,values,dd=day):
	vs = filter(lambda x : x[0] < (t+dd) and x[0] > (t-dd),values)
	if len(vs) == 0:
		return 0
	ds, v = zip(*vs)
	return sum(v)

#Values per day 		
def vpd(values):
	vals = []
	for da in allthedays:
		vals.append((da,getValueAtTime(da,values)))
	return vals

#values on day
def vod(values,dd=day):
	vals = []
	for da in allthedays:
		vals.append((da,getOnDate(da,values,dd=dd)))
	return vals
		
def getcache(cfile,key):
	
	shelf = shelve.open(cfile)
	val = shelf[key]
	shelf.close()
	return val

def chpd(cfile):
	ch = vod(getcache(cfile,"chn"))
	ch = dict(ch)
	vals = []
	for da in allthedays:
		vals.append(1.0*ch[da])
	return vals

#Change per week
def chpw(cfile):
	ch = vod(getcache(cfile,"chn"),dd=week)
	ch = dict(ch)
	vals = []
	for da in allthedays:
		vals.append(1.0*ch[da])
	return vals
		
def sizepd(cfile):
	size = vpd(getcache(cfile,"size"))
	size = dict(size)
	vals = []
	for da in allthedays:
		vals.append(1.0*size[da])
	return vals
	
def uttdperc(cfile):
	uttd = vpd(getcache(cfile,"uttd"))
	size = vpd(getcache(cfile,"size"))
	uttd = dict(uttd)
	size = dict(size)
	
	vals = []
	for da in allthedays:
		vals.append(1.0*uttd[da]/size[da])
	return vals


def uttdpd(cfile):
	uttd = vpd(getcache(cfile,"uttd"))
	das,uttd = zip(*uttd)
	return uttd

def multimeanstd(values):
	imean = numpy.mean(values,axis=0)
	istd = numpy.std(values,axis=0)
	imeanpstd = map(lambda x : x[0]+x[1], zip(imean,istd)) 
	imeanmstd = map(lambda x : x[0]-x[1], zip(imean,istd)) 
	return imean,istd,imeanpstd,imeanmstd


folder = "cache/q1a"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))
installs = filter(lambda x : os.path.basename(x).startswith("i"),files);
uinstalls = filter(lambda x : os.path.basename(x).startswith("u"),files);

always = os.path.join(folder,"alwaysupdate.user")
never = os.path.join(folder,"never.user")

def plotuttdpc():
	#UPTODATE Distance per component?

	#What is the difference between always updating and never updating?


	auttdpc = uttdperc(always)
	nuttdpc = uttdperc(never)

	pylab.figure(1)

	cc = ColorConverter()

	ivals = map(lambda x : uttdperc(x),installs)
	imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)

	pylab.plot(pallthedays,imean,color='g',label="Install Mean uttdpc +-1std ")
	pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor='g', alpha=0.5)
		        
	#for i in installs:
	#	das,iuttdpc = uttdperc(i)
	#	pylab.plot(das,iuttdpc,color=())

	uivals = map(lambda x : uttdperc(x),uinstalls)
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(uivals)
	pylab.plot(pallthedays,uimean,color='blue',label="Update and Install Mean uttdpc +-1std")
	pylab.fill_between(pallthedays, uimeanpstd, uimeanmstd, facecolor='blue', alpha=0.5)

	#for i in uinstalls:
	#	das,iuttdpc = uttdperc(i)
	#	pylab.plot(das,iuttdpc,color='black')

	pylab.plot(pallthedays,auttdpc,color='black',label="Always Update uttdpc")

	pylab.plot(pallthedays,nuttdpc,color='r',label="Never Update uttdpc")	

	pylab.legend(loc="upper left")

	print "Last Always update uttdpc", auttdpc[-1]
	print "Last Never update uttdpc", nuttdpc[-1]

	saveFigure("q1auttd")
	#Static information
	#Even if systems always update, they will become outof date, (likely due to the core components of the system having keep constraints to maintain system integrity).
	#The Out of dateness increases dramatically around the 10.04 release, more so for those who do not update.
	#After a year, systems that dont update become about 2uttdpc out of date, components in the system have on average 2.1 better component available.
	#After a year, systems that do update are about .72 uttdpc out of date.


	#Comparison
	#As you can see in this figure Installing components has only a slight effect on the outofdateness of a system w.r.t. updating.
	#That is, the uttd per component metric for "installing and not updating" and the "not installing and not updating" users are very similar.
	#Also the "installing and updating" and the "not installing and updating" users are also very similar.
	#This leads to the conclusions that installing components does not have a large effect on uttdpc.

def plotsize():
	#Size of the system, of install and uinstall combined
	pylab.figure(2)
	ivals = map(lambda x : sizepd(x),installs+uinstalls)
	imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
	pylab.plot(pallthedays,imean,color='g',label="Install and Update and Install Mean Size")
	#pylab.fill_between(das, imeanpstd, imeanmstd, facecolor='g', alpha=0.5)


	pylab.legend(loc="upper left")

	saveFigure("q1size")
	#You can see at the very begining the gradient is steper as the installation requests install common components. As the system evolves the common components have already been installed, making the increasing of the system require less installs


def plotchangepd():
	#CHANGE

	#What are the common packages that are installed? By including these in the base system, it will lower the necessary change to the system as users install other packages.

	#Hypothesis: installing lots will decrease change over time, as common dependencies will be installed!
	fig = pylab.figure(3)
	
	bins = [0,1,2,3,4,5,10,20,40,60,80,100]
	locs = numpy.array(range(len(bins)-1))
	ticks = ["0","1","2","3","4","5-10","10-20","20-40","40-60","60-80","80-100"]
	v = chpd(always)
	hist = numpy.histogram(v,bins=bins)
	ax1 = fig.add_subplot(311)
	ax1.bar(locs,hist[0],color='black')
	pylab.xticks(locs+1/2., ticks)
	
	hists = []
	for v in map(lambda x : chpd(x),installs):
		hists.append(numpy.histogram(v,bins=bins)[0])
	means = numpy.mean(hists,axis=0)
	ax1 = fig.add_subplot(312)
	ax1.bar(locs,means,color='g')
	pylab.xticks(locs+1/2., ticks)
	
	hists = []
	for v in map(lambda x : chpd(x),uinstalls):
		hists.append(numpy.histogram(v,bins=bins)[0])
	means = numpy.mean(hists,axis=0)
	ax1 = fig.add_subplot(313)
	plt = ax1.bar(locs,means,color='blue')
	
	pylab.xticks(locs+1/2., ticks)

	saveFigure("q1change")
	#You can see that nearly 80 days out of 365 when updating every day no changes are made! That means that updating everyday is excessive!
	#When updating every day it can also be seen that most of the updates are belwo 10, with about 40/365 updates changing between 10 and 20, and less than 20 days changing between 2- amd 40.
	#You can see in the install only graph that just about all changes are below 10
	#Updating and installing will change the system by between 5 and 20 components. More than half the days of the year the system mean change is above 5.
	#This is a significant amount of change the system goes through, and is the estimated max amount of change that 
	
	
	pylab.figure(4)
	ivals = map(lambda x : chpw(x),installs)
	imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
	pylab.plot(pallthedays,imean,color='g',label="Install Mean Change per week")
	pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor='g', alpha=0.5)
	
	#ivals = map(lambda x : chpw(x),uinstalls)
	#imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
	#pylab.plot(palltheweeks,imean,color='blue',label="Update Install Mean Change per week")
	#pylab.fill_between(palltheweeks, imeanpstd, imeanmstd, facecolor='blue', alpha=0.5)
	
	saveFigure("q1changepweek")
#plotuttdpc()
#plotsize()
plotchangepd()







#Failure is interesting
#Reasons for Failure:
#Installs
#req=install: zygrib

#req=install: colorcode

#req=install: chromium-browser

#req=install: colorcode
#req=install: fotoxx
#req=install: fceux

#req=install: imageshack-uploader
#req=install: swell-foop

#req=install: aqemu

#req=install: q4wine

#req=install: g2ipmsg
#req=install: swell-foop
#req=install: sweethome3d

#req=install: transmageddon

#req=install: gnome-paint

#req=install: freecad
#req=install: fceux
#req=install: q4wine
#req=install: parole


#U and I
#An interesting but rare case (occured once) under certain conditions installing "Chromium Browser" requires two versions of the package libc6 installed, this means the following upgrade fails, and the intall after that removes chomium-browser from the system with the superfluous libc6 package.





