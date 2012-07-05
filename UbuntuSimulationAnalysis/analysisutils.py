import os
import shelve
from matplotlib import dates
import numpy
import datetime
import pylab 

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

def saveFigure(name,size=(15,9)):
	pylab.gcf().set_size_inches(size[0],size[1])
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

#updated names till time
def updtt(cfile):
	ch = vtd(getcache(cfile,"upd"))
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
