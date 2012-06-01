#!/usr/bin/python

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
allcomps = cudfpkg.createProfileChangeRequest("1288868400.0",nameversiononly=False)


start = 1256814000 #one day before first action
end = 1288410600
#end = 1256814000 +(10*24*60*60)

#q1afolder = "q1a"
#q1afiles = os.listdir(q1afolder)
#q1afiles = map(lambda x: os.path.join(q1afolder,x),q1afiles)




def cacheuser(userfile):
	print "cache",userfile
	cudfs = utils.processSolutionsFolder(userfile+".sols")
	cudfs[start] = initsys #add original system
	#end = 1256814000+(24*60*60*10)
	uttd = "uttd"
	if not iscached(userfile,uttd):
		print "Add uptodate distance to ",userfile
		cache(userfile,utils.getUptoDateDistance(start,end,cudfs,allcomps),uttd)
	
	upd = "upd"
	if not iscached(userfile,upd):
		print "Add uptodated packs ",userfile
		cache(userfile,utils.updatedPackages(start,end,cudfs),upd)
	
	changedname = "chn"
	if not iscached(userfile,changedname):
		print "Add changed names ",userfile
		cache(userfile,utils.changedNames(cudfs),changedname)
	
	siz = "size"
	if not iscached(userfile,siz):
		print "Add size ",userfile
		cache(userfile,utils.size(cudfs),siz)
	
	newnames = "ncn"
	if not iscached(userfile,newnames):
		print "Add new Names ",userfile
		cache(userfile,utils.newNames(cudfs),newnames)
	

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

users = []
users += filter(lambda x: x.endswith(".user"),map(lambda x : os.path.join("q1a",x),os.listdir("q1a")))
users +=filter(lambda x: x.endswith(".user"), map(lambda x : os.path.join("q1b",x),os.listdir("q1b")))

for u in sorted(users):
	print u
	cacheuser(u)



