#!/usr/bin/python
from cudfpy import cudfpkg
import numpy

rep = cudfpkg.createProfileChangeRequest("1288868400.0",nameversiononly=False)
initsys = cudfpkg.createProfileChangeRequest("9.10.cudf",nameversiononly=True)

start = 1256814000 #one day before first action
end = 1288410600
print "first"
ups = {}
for pn in initsys.getPackageNames():
	apack = initsys.getPackagesThatSatisfy((pn,-1,""))[0]
	ups[pn] = 0
	packs = rep.getPackagesThatSatisfy((pn,-1,""))
	for package in packs:
		if package.date >= start and package.date <= end and package.version > apack.version:
			ups[pn] = ups[pn] + 1

print "First Value", numpy.mean(ups.values())
