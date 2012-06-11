#!/usr/bin/python

import gc
import os
from cudfpy import cudfpkg
import timeit
import pylab
import utils
import numpy as np
import shelve

from multiprocessing import Pool

print "Load init stuff"

initsys = cudfpkg.createProfileChangeRequest("9.10.cudf",nameversiononly=True)
allcomps = None


start = 1256814000 #one day before first action
end = 1288410600
#end = 1256814000 +(10*24*60*60)

#q1afolder = "q1a"
#q1afiles = os.listdir(q1afolder)
#q1afiles = map(lambda x: os.path.join(q1afolder,x),q1afiles)


def doit(cudfs,userfile,reqComps=False):
	global allcomps
	if reqComps and allcomps == None:
		print "load Allcomps"
		allcomps = cudfpkg.createProfileChangeRequest("1288868400.0",nameversiononly=False)
	if cudfs == None:
		cudfs = utils.processSolutionsFolder(userfile+".sols")
		cudfs[start] = initsys #add original system
	return cudfs
	

	
def cacheuser(userfile):
	print "cache",userfile
	cudfs = None
	#end = 1256814000+(24*60*60*10)
	uttd = "uttd"
	if not iscached(userfile,uttd):
		cudfs = doit(cudfs,userfile,reqComps=True)
		print "Add uptodate distance to ",userfile
		cache(userfile,utils.getUptoDateDistance(start,end,cudfs,allcomps),uttd)
	
	upd = "upd"
	if not iscached(userfile,upd):
		cudfs = doit(cudfs,userfile)
		print "Add uptodated packs ",userfile
		cache(userfile,utils.updatedPackages(start,end,cudfs),upd)
	
	changedname = "chn"
	if not iscached(userfile,changedname):
		cudfs = doit(cudfs,userfile)
		print "Add changed names ",userfile
		cache(userfile,utils.changedNames(cudfs),changedname)
	
	siz = "size"
	if not iscached(userfile,siz):
		cudfs = doit(cudfs,userfile)
		print "Add size ",userfile
		cache(userfile,utils.size(cudfs),siz)
	
	newnames = "ncn"
	if not iscached(userfile,newnames):
		cudfs = doit(cudfs,userfile)
		print "Add new Names ",userfile
		cache(userfile,utils.newNames(cudfs),newnames)
	
	remnames = "rcn"
	if not iscached(userfile,remnames):
		cudfs = doit(cudfs,userfile)
		print "Add removed Names ",userfile
		cache(userfile,utils.removedNames(cudfs),remnames)
		
	

def iscached(userfile, key):
	cfile = os.path.join("cache",userfile)
	if not os.path.exists(cfile):
		return False
	
	ret = False
	shelf = shelve.open(cfile)
	if key in shelf:
		ret = True
	shelf.close()
	return ret
	
def cache(userfile,val,key):
	
	cfile = os.path.join("cache",userfile)
	if not os.path.exists(os.path.dirname(cfile)):
		os.makedirs(os.path.dirname(cfile))
	shelf = shelve.open(cfile)
	shelf[key] = val
	shelf.close()


#def fixuttd(userfile):
#	cfile = os.path.join("cache",userfile)
#	shelf = shelve.open(cfile)
#	uttd = shelf["uttd"]
#	dates = range(start,end,(24*60*60))
#	nuttd = zip(dates,uttd)
#	shelf["uttd"] = nuttd
#	shelf.close()
	
#cacheuser("q1a/alwaysupdate.user")
#exit()

def getUsers(folder):
	return filter(lambda x: x.endswith(".user"),map(lambda x : os.path.join(folder,x),os.listdir(folder)))
	
users = []
#users += getUsers("q1c")
#users += getUsers("q2c")
#users += getUsers("q3")
#users += getUsers("q4a")
#users += getUsers("q1a")
users += getUsers("q1b")
#users += getUsers("q5a")

for u in sorted(users):
	print u
	cacheuser(u)
	gc.collect()



