#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter
import datetime

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

def getValueTillTime(t,values):
	total = 0
	for ds,v in sorted(values,key=lambda x: x[0]):
		if ds <= t:
			total += v
		else :
			return total
	return total
						
#Returns sum of values plus minus 12 hours of time (e.g. change)
def getOnDate(t,values,dd=day):
	vs = filter(lambda x : x[0] <= (t+(day)) and x[0] > (t),values)
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

#values till day
def vtd(values):
	vals = []
	for da in allthedays:
		vals.append((da,getValueTillTime(da,values)))
	return vals
	
#values on day
def vod(values,dd=day):
	vals = []
	for da in allthedays:
		vals.append((da,getOnDate(da,values)))
	return vals
		
def getcache(cfile,key):
	shelf = shelve.open(cfile)
	val = shelf[key]
	shelf.close()
	return val

#change till time
def chtt(cfile):
	ch = vtd(getcache(cfile,"chn"))
	ch = dict(ch)
	vals = []
	for da in allthedays:
		vals.append(1.0*ch[da])
	return vals

#new names till time
def nntt(cfile):
	ch = vtd(getcache(cfile,"ncn"))
	ch = dict(ch)
	vals = []
	for da in allthedays:
		vals.append(1.0*ch[da])
	return vals

#removed names till time
def rempd(cfile):
	ch = vod(getcache(cfile,"rcn"))
	ch = dict(ch)
	vals = []
	for da in allthedays:
		vals.append(1.0*ch[da])
	return vals
	
#change per day		
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

def plotuttdpd():
	#UPTODATE Distance per component?

	#What is the difference between always updating and never updating?



	pylab.figure(1)

	auttdpd = uttdpd(always)
	nuttdpd = uttdpd(never)

	ivals = map(lambda x : uttdpd(x),installs)
	imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
	pylab.plot(pallthedays,imean,color='g',label="Install Mean uttd +-1std ")
	pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor='g', alpha=0.5)
		        
	#for i in installs:
	#	das,iuttdpd = uttdperc(i)
	#	pylab.plot(das,iuttdpd,color=())

	uivals = map(lambda x : uttdpd(x),uinstalls)
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(uivals)
	pylab.plot(pallthedays,uimean,color='blue',label="Update and Install Mean uttd +-1std")
	pylab.fill_between(pallthedays, uimeanpstd, uimeanmstd, facecolor='blue', alpha=0.5)

	#for i in uinstalls:
	#	das,iuttdpd = uttdperc(i)
	#	pylab.plot(das,iuttdpd,color='black')

	pylab.plot(pallthedays,auttdpd,color='black',label="Always Update uttd")

	pylab.plot(pallthedays,nuttdpd,color='r',label="Never Update uttd")	

	pylab.legend(loc="upper left")

	print "Last Always update uttdpd", auttdpd[-1]
	print "Last Never update uttdpd", nuttdpd[-1]

	saveFigure("q1auttd")
	
	


	pylab.figure(2)

	auttdpc = uttdperc(always)
	nuttdpc = uttdperc(never)
	
	ivals = map(lambda x : uttdperc(x),installs)
	imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
	pylab.plot(pallthedays,imean,color='g',label="Install Mean uttdpc +-1std ")
	pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor='g', alpha=0.5)
		        
	#for i in installs:
	#	das,iuttdpd = uttdperc(i)
	#	pylab.plot(das,iuttdpd,color=())

	uivals = map(lambda x : uttdperc(x),uinstalls)
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(uivals)
	pylab.plot(pallthedays,uimean,color='blue',label="Update and Install Mean uttdpc +-1std")
	pylab.fill_between(pallthedays, uimeanpstd, uimeanmstd, facecolor='blue', alpha=0.5)

	#for i in uinstalls:
	#	das,iuttdpd = uttdperc(i)
	#	pylab.plot(das,iuttdpd,color='black')

	pylab.plot(pallthedays,auttdpc,color='black',label="Always Update uttdpc")

	pylab.plot(pallthedays,nuttdpc,color='r',label="Never Update uttdpc")	

	pylab.legend(loc="upper left")

	print "Last Always update uttdperc", auttdpc[-1]
	print "Last Never update uttdpperc", nuttdpc[-1]

	saveFigure("q1auttdperc")
	
	
	#Static information
	#Even if systems always update, they will become outof date, (likely due to the core components of the system having keep constraints to maintain system integrity).
	#The Out of dateness increases dramatically around the 10.04 release, more so for those who do not update.
	#After a year, systems that dont update become thousands uttd out of date.
	#After a year, systems that do update are about 1000 uttd out of date.
	#The standard deviation of the Update and Install uttd increases significantly after May. 
	#This could be because the release means the  
	 

	#Comparison
	#As you can see in this figure Installing components has only a slight effect on the outofdateness of a system w.r.t. updating.
	#That is, the uttd per component metric for "installing and not updating" and the "not installing and not updating" users are very similar.
	#Also the "installing and updating" and the "not installing and updating" users are also very similar.
	#This leads to the conclusions that installing components does not have a large effect on uttdpd.

def analysesize():
	#Size of the system, of install and uinstall combined
	
	ivals = map(lambda x : sizepd(x),installs+uinstalls)
	imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
	sizepdc = numpy.array(imean) - 1270

	print 4,sizepdc[-1]/len(allthedays)
	#You can see at the very begining the gradient is steper as the installation requests install common components. As the system evolves the common components have already been installed, making the increasing of the system require less installs
	#by taking the first 10 installations of every install only user we collect the most commonly installed packages.
	#libaudio2 for example is installed in every component system after 10 random installs. And it cannot be directly requested for install.
	#libqt3-mt, libavahi-qt3-1, kdelibs4c2a, liblua50, kdelibs-data, liblualib50, are all installed in 28 of the systems. These are also seen as likely install candadiates.
	
	files = []
	for f in filter(lambda x: x.endswith(".out") and x.startswith("i"), os.listdir("q1a")):
		files.append(os.path.join("q1a",f))
	
	print len(files)
	def installedPacks(outfile):
		lines = open(outfile).readlines()
		ilines = filter(lambda x: x.startswith("Install: "), lines)[:10]
		ilines =  map(lambda x : filter(lambda x: not x.startswith("("), x[len("Install: "):].strip().split()),ilines)
		
		packages = set([p for sublist in ilines for p in sublist])
		return list(packages)
	
	ipacks = map(installedPacks,files)

	allpacks = []
	for i in ipacks:
		allpacks += i
		
	from collections import defaultdict
	
	coutnerd = defaultdict(int)
	for p in allpacks:
		coutnerd[p] +=1
	

def plotchange():
	fig = pylab.figure(3)
	
	pylab.plot(pallthedays,chtt(always), color="black", label="Always Update Mean change")
	
	uivals = map(lambda x : chtt(x),installs)
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(uivals)
	pylab.plot(pallthedays,uimean,color='green',label="Install Mean change +-1std")
	pylab.fill_between(pallthedays, uimeanpstd, uimeanmstd, facecolor='green', alpha=0.5)
	
	
	uivals = map(lambda x : chtt(x),uinstalls)
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(uivals)
	pylab.plot(pallthedays,uimean,color='blue',label="Update and Install Mean change +-1std")
	pylab.fill_between(pallthedays, uimeanpstd, uimeanmstd, facecolor='blue', alpha=0.5)
	
	pylab.legend(loc="upper left")
	
	saveFigure("q1change")
	#To analyse this information first the always update change is looked at.
	#The first point to note is the gradients along the curve, there are two, 
	#1) the gradients before and after the 10.04 release, 2) one during the release cycle, between March and April of 2010. 
	#On march 1st 469 components had changed, on april 30th it was 1000, August 31st 1373.
	#During the release the change is 266 changes per month, and previously to that it is at about 100 changes per month (117 before, and 93 after).
	#So the amount of components required to be upgraded doubles during releases.
	
	#Next looking at the installes curve it can be seen that it has a constant gradient.
	#This gradient result in the mean change of 1137
	#The standard deviation increases because of catastrauphic requests.
	
	#The update and install change is similar to the combination of the two curves to always update and install.
	#Change increases during the release months, like in the always update curve, and the variation increases after this period like in the install period.
	#However the effect of continually installing leads the effect of these to be exaggreated,  
	# as more components are intalled more are required to be updated.
	#This makes the total change more than the sum of the two other curves.
	
	
	#Difference between always update install and update
	fig = pylab.figure(4)
	
	uivals = map(lambda x : nntt(x),installs)
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(uivals)
	pylab.plot(pallthedays,uimean,color='green',label="Update and Install Mean New names +-1std")
	pylab.fill_between(pallthedays, uimeanpstd, uimeanmstd, facecolor='green', alpha=0.5)
	
	
	uivals = map(lambda x : nntt(x),uinstalls)
	uimean,uistd,uimeanpstd,uimeanmstd = multimeanstd(uivals)
	pylab.plot(pallthedays,uimean,color='blue',label="Update and Install Mean change +-1std")
	pylab.fill_between(pallthedays, uimeanpstd, uimeanmstd, facecolor='blue', alpha=0.5)
	
	pylab.legend(loc="upper left")
	
	saveFigure("q1newnames")

	
def findcatfailures():
	#looking for failure type 2, where a massive change occurs
	uivals = zip(installs,map(lambda x : rempd(x),installs))
	for name,ui in uivals:
		for date, remv in zip(allthedays,ui):
			if remv >= 100:
				print date,datetime.date.fromtimestamp(date),name	
	
	uivals = zip(uinstalls,map(lambda x : rempd(x),uinstalls))
	for name,ui in uivals:
		for date, remv in zip(allthedays,ui):
			if remv >= 100:
				print date,datetime.date.fromtimestamp(date),name	
	
	
	#all but one of the soft failures occur around the month of the april release
	#All that failed are followed by another large removal of components.
	
	# 2010-02-22 cache/q1a/i3.user
	# 2010-04-18 cache/q1a/i5.user
	# 2010-04-12 cache/q1a/i1.user
	# 2010-04-21 cache/q1a/uandi13.user
	# 2010-05-04 cache/q1a/uandi16.user
	# 2010-05-11 cache/q1a/uandi26.user
	# 2010-04-19 cache/q1a/uandi28.user
	# 2010-04-12 cache/q1a/uandi1.user

	#i1.user req=install: gnome-btdownload 1 day later removed
	#uandi1 req=install: gnome-btdownload not removed!
	
	
	#i3.user "install: python-wxglade" 8 days later python-wxglade is removed
	#i5.user req=install: soundconverter, 10 days later it is removed
	
	#uandi13 req=install: python-wxglade  removed next day
	#uandi16 req=install: istanbul removed 5 days later
	#uandi26 req=install: streamtuner not removed
	#uandi28 install: soundconverter not removed
	
	#from this information it can be seen that although more likely to have a soft failure the uandi users are more likley to adapt without removing the reason for the failure.
	#this can be seen in the i1 and uani1 comparison.
	#Some components like "python-wxglade" are more likely to cause problems. Though do not always, as 14 total attempted to install "python-wxglade", and only 2 had a soft failure.
			
#plotuttdpd()

#plotchange()

findcatfailures()

#pylab.show()

#There are two levels of failure that we would like to discuss.
#First a request that to be satisfied has to alter the system significantly.
#Second, a request that cannot be satisfied by the system.
#For the second type there are two types of failure, either failure to install, or failure to update.

#Catrastraphic change (Change that is above normal (TODO))
#(i1 i3 and i5 all have a catrastraphic change, but only uandi1 has a catratrauphic change. This suggests that because of the constantly updating the changes made in i3 and i5 were averted. Another reason to upgrade.

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


#Update failure
#An interesting but rare case (occured once) under certain conditions installing "Chromium Browser" requires two versions of the package libc6 installed, this means the following upgrade fails, and the intall after that removes chomium-browser from the system with the superfluous libc6 package.





