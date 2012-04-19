#!/usr/bin/python
import apt_pkg #useful version_compare parse_depends
apt_pkg.init()

import os
import pickle
import sys
import getopt
from debpkg import DebianPackage
from cudfpkg import CUDFPackage

__doc__ = """
Deb2Cudf script converst debian constrol files to CUDF format as defined by the mancoosi organisation
deb2cudf [iom|ioc]
i input control file
o output cudf (or version map file) file if none then to console (set to the same meta info with dates)
m the versionMapFile used (to map versions to integers)
c create version map file from a set of packages file (this is a pickel of a map from String to Int)
"""
_package = "Package: "
_version = "Version: "
_depends = "Depends: " 
_pre_depends= "Pre-Depends: "
_provides = "Provides: "
_recommends = "Recommends: "
_suggests = "Suggests: "
_enhances = "Enhances: "
_breaks = "Breaks: "
_conflicts = "Conflicts: "

_priotity = "Priority: "
_description = "Description: "
_section = "Section: "
_installed_size = "Installed-Size: "
_maintainer = "Maintainer: "
_source = "Source: "
_architecture = "Architecture: "
_original_maintainer = "Original-Maintainer: "
_replaces = "Replaces: "
_homepage = "Homepage: "


def stripVersions(deps):
	vs = []
	for a in deps:
		for o in a:
			if(o[1] != ""):
				vs.append(o[1])
	return tuple(vs)

def createVersionMap(inputFile,outputFile):
	versionSet = set()
	
	f = open(inputFile,"r")
	for line in f:
		if line.startswith(" " or "\t") or line.strip() == "" :
				None
		elif line.startswith(_package):
				None
		elif line.startswith(_version):
				versionSet.add(line[len(_version):].strip())
		elif line.startswith(_depends):
				deps = apt_pkg.parse_depends(line[len(_depends):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_pre_depends):
				deps = apt_pkg.parse_depends(line[len(_pre_depends):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_provides):
				deps = apt_pkg.parse_depends(line[len(_provides):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_recommends):
				deps = apt_pkg.parse_depends(line[len(_recommends):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_suggests):
				deps = apt_pkg.parse_depends(line[len(_suggests):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_enhances):
				deps = apt_pkg.parse_depends(line[len(_enhances):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_breaks):
				deps = apt_pkg.parse_depends(line[len(_breaks):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_conflicts):
				deps = apt_pkg.parse_depends(line[len(_conflicts):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_priotity):
				None
		elif line.startswith(_description):
				None
		elif line.startswith(_section):
				None
		elif line.startswith(_installed_size):
				None
		elif line.startswith(_maintainer):
				None
		elif line.startswith(_source):
				None
		elif line.startswith(_architecture):
				None
		elif line.startswith(_original_maintainer):
				None
		elif line.startswith(_replaces):
				deps = apt_pkg.parse_depends(line[len(_replaces):].strip())
				vs = stripVersions(deps)
				versionSet |= set(vs)
		elif line.startswith(_homepage):
				None
		else : None
		
	
	#sort the version set into a list
	versionList = list(versionSet)
	versionList.sort(apt_pkg.version_compare)
	
	versionMap = {}
	for i in range(len(versionList)) :
		versionMap[versionList[i]] = i
	pickle.dump(versionMap,open(outputFile,"w"))
	


def convertProvidesToCUDF(provs):
	"""This takes a set of virtual packages and states they are provided"""
	ret = []
	for d in provs:
		dret = []
		for (n,v,c) in d:
			dret.append(("virtual--" + n,None,c))
		ret.append(dret)
	return ret		

def convertDependsToCUDF(deps, vm):
	ret = []
	for d in deps:
		dret = []
		for (n,v,c) in d:
			#Takes virtual packages into account
			if v == "":
				dret.append(("virtual--" + n,None,c))
			dret.append((n,vm[v],c))
		ret.append(dret)
	return ret

def convertConflictsToCUDF(deps, vm):
	ret = []
	for d in deps:
		for (n,v,c) in d:
			#Takes virtual packages into account
			if v == "":
				ret.append([("virtual--" + n,None,c)])
			ret.append([(n,vm[v],c)])
	return ret
	
def createCudfFile(inputFile,outputFile,vm):	
	f = open(inputFile,"r")
	#TODO better
	vm[""] = None
	packs = []
	#break into packages	
	lines = []
	for line in f:
		if(line.strip() == ""):
			if(lines != []):
				packs.append(DebianPackage(lines))
			lines = []
		else: lines.append(line)	
	if(lines != []):
		packs.append(DebianPackage(lines))
	
	#Remove packages without name or version
	print len(packs)	
	packs = filter(lambda x: x.version != "" and x.name != "", packs)
	print len(packs)
	
	#list virtual packages

#	vps = set()
#	for p in packs :
#		for prov in p.provides :
#			for provc in prov :
#				vps.add(provc[0])
#	for v in vps:
#		print v
	
	cpacks = []
	of = open(outputFile,"w")
	for p in packs:
		c = CUDFPackage()
		c.version = vm[p.version]
		c.name = p.name
		#depends = depends + pre-depends
		c.depends = convertDependsToCUDF(p.depends,vm) + convertDependsToCUDF(p.pre_depends,vm)
		c.provides = convertProvidesToCUDF(p.provides)
		c.recommends = convertDependsToCUDF(p.recommends,vm) + convertDependsToCUDF(p.suggests,vm)
		c.conflicts = convertConflictsToCUDF(p.conflicts,vm)
		c.arch = p.arch
		if(p.priority == "required"):
			c.keep = "package"
		else:	
			c.priority = p.priority

		#add the date of the file
		(mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime) = os.stat(inputFile)
		c.date = mtime

		of.write(str(c))
		of.write("\n")
	of.close()

def main():
	try:
		opts, args = getopt.getopt(sys.argv[1:], "ci:o:m:h", [])
	except getopt.error, msg:
		print msg
		print "for help use -h"
		sys.exit(2)

	outputFile = None
	inputFile = None
	versionMapFile = None
	cVersionMap = False

	# process options
	for o, a in opts:
		if o == ("-h"):
			print __doc__
			sys.exit(0)
		elif o == "-c" :
			cVersionMap = True
		elif o == "-i" :
			inputFile = a
		elif o == "-o":
			outputFile = a
		elif o == "-m" :
			versionMapFile = a
	
	#validate input
	if(inputFile == None or outputFile == None):
		print("io problem")
		sys.exit(0)

	if((versionMapFile != None) == cVersionMap):
		print "Probelm"
		sys.exit(0)

	# process arguments
	if(cVersionMap):
		createVersionMap(inputFile,outputFile)
	elif(versionMapFile != None):
		createCudfFile(inputFile,outputFile,pickle.load(open(versionMapFile)))

if __name__ == "__main__":
	main()
