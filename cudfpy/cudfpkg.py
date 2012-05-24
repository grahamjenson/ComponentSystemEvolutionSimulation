import gzip
import os
_preamble = "preamble:"
_request = "request:"

_package = "package: "
_version = "version: "
_depends = "depends: " 
_provides = "provides: "
_recommends = "recommends: "
_conflicts = "conflicts: "
_installed = "installed: "

_priotity = "priority: "
_keep = "keep: "
_architecture = "architecture: "
_date = "date: "

gteq = ">="
lteq = "<="
neq = "!="
lt = "<"
gt = ">"
eq = "="


def createProfileChangeRequest(inputFile,nameversiononly=False):
	"""Takes a file with multiple cudf packages in it, and outputs them"""
	f = None
	if inputFile.endswith(".gz"):
		f = gzip.open(inputFile,'r')
	else:
		f = open(inputFile,"r")
		
		
	tls = []
	pcr = ProfileChangeRequest()
	line = f.readline()
	while line:
		if line.strip() == "" :
			if tls != []:
				if tls[0].startswith(_package):
					pkg = CUDFPackage(tls,basic=nameversiononly)
						
					pcr.add(pkg)
				elif tls[0].startswith(_preamble) :
					pcr.preamble = reduce(lambda x,y: x.strip() + "\n" + y.strip(), tls)
				elif tls[0].startswith(_request) :
					pcr.request = reduce(lambda x,y: x.strip() + "\n" + y.strip(), tls)

			tls = []
		else:
			if not line.startswith("#"):
				tls.append(line)
		line = f.readline()
	if(tls != []):
		if tls[0].startswith(_package) :
			pcr.add(CUDFPackage(tls))
		elif tls[0].startswith(_preamble) :
			pcr.preamble = reduce(lambda x,y: x.strip() + "\n" + y.strip(), tls)
		elif tls[0].startswith(_request) :
			pcr.request = reduce(lambda x,y: x.strip() + "\n" + y.strip(), tls)
		
		
	return pcr

def findPackages(packs,(name,version,equality)):
	if name not in packs: return []	
	if equality == "=":
		equality = "=="
	elif equality == "":
		version = -1
	ret = []
	#Any version of -1 is automatically in, only for features
	if -1 in packs[name] : ret.extend(packs[name][-1])

	if(version == -1):	
		ret.extend(packs[name].values())
	else:
		ret.extend(filter(lambda x : eval(str(x.version) + equality + str(version)),packs[name].values()))
	return ret

class ProfileChangeRequest(object):

	def __init__(self):
		self.installed = {}
		self.ninstalled = {}
		self.features = {}
		self.nfeatures = {}
		self.cache = (self.installed, self.ninstalled, self.features, self.nfeatures)
		self.preamble = ""
		self.request = ""

	def add(self,p):
		if(p==None or p.name == "" or p.version == -1):
			return

		val = 0 #looking at installs
		if(not p.installed):
			val = 1 # looking at not installed
		
		if p.name not in self.cache[val]:
			self.cache[val][p.name] = {}
			
		#validation
		#if p.version in self.cache[val][p.name]:
		#	print "Warning, multiple package version included", (p.name,p.version)
		#if p.name in self.cache[val^1] and p.version in self.cache[val^1][p.name]:
		#	print "Warning, package installed and not installed", (p.name,p.version)
			
		#add it
		self.cache[val][p.name][p.version] = p
		
		for vp in [x[0] for x in p.provides]:
			name = vp[0]
			if vp[1] == "":
				version = -1
			else:
				version = int(vp[1])
			if name not in self.cache[val+2]:
				self.cache[val+2][name] = {}
			self.cache[val+2][name][version] = p
			
	
	def getPackagesThatSatisfy(self,constraint,onlyinstalled = False):
		ret = set()
		ret.update(findPackages(self.installed,constraint))
		ret.update(findPackages(self.features,constraint))
		if(not onlyinstalled):
			ret.update(findPackages(self.ninstalled,constraint))
			ret.update(findPackages(self.nfeatures,constraint))
		return list(ret)

	def getUniverse(self):
		return [p for a in self.installed.values() for p in a.values()] + [p for a in self.ninstalled.values() for p in a.values()]
	
	def getInstalled(self):
		return [p for a in self.installed.values() for p in a.values()]
	
	def getPackageNames(self,onlyinstalled = False):
		if onlyinstalled:
			return self.installed.keys()
		else:
			return self.installed.keys() + self.ninstalled.keys()

	def __str__(self):
		sb = ""
		el = "\n"
		sb += self.preamble + el + el
		sb += "#List of Installed packages" + el
		for pack in sorted([p for vs in self.installed.values() for p in vs.values()],key=lambda x: x.nv()):
			sb += str(pack)
			sb += el
		
		sb += "#List of Not Installed packages" + el
		for pack in sorted([p for vs in self.ninstalled.values() for p in vs.values()],key=lambda x: x.nv()):
			sb += str(pack)
			sb += el

		sb += self.request + el

		return sb
		
	def toMap(self, onlyinstalled=False):
	
		m = {}
		for n in self.installed.keys():
			if n not in m : m[n] = []
			
			for v in self.ninstalled[n]:
				m[n].append(v)
				
		if not onlyinstalled:
			for n in self.ninstalled.keys():
				if n not in m : m[n] = []
			
				for v in self.ninstalled[n]:
					m[n].append(v)
		return m

def parsePackageFormula(line):
	ret = []
	ands = line.split(",")
	for a in ands:
		tmp = []
		ors = a.split("|")
		for o in ors:
			if len(o.split(gteq)) == 2:
				pf = o.split(gteq)
				tmp.append((pf[0].strip(),int(pf[1].strip()),gteq))	
			elif len(o.split(lteq)) == 2:
				pf = o.split(lteq)
				tmp.append((pf[0].strip(),int(pf[1].strip()),lteq))
			elif len(o.split(neq)) == 2:
				pf = o.split(neq)
				tmp.append((pf[0].strip(),int(pf[1].strip()),neq))
			elif len(o.split(lt)) == 2:
				pf = o.split(lt)
				tmp.append((pf[0].strip(),int(pf[1].strip()),lt))
			elif len(o.split(gt)) == 2:
				pf = o.split(gt)
				tmp.append((pf[0].strip(),int(pf[1].strip()),gt))
			elif len(o.split(eq)) == 2:
				pf = o.split(eq)
				tmp.append((pf[0].strip(),int(pf[1].strip()),eq))
			else : #no equality
				tmp.append((o.strip(),0,""))
		ret.append(tmp)
	return ret

def strPackageFormula(alist):
	sb = ""
	ad = ""
	for olist in alist:
		od = ""
		sb += ad
		for pf in olist:
			sb += od
			if(pf[2] != ""):
				sb += pf[0] + " " + pf[2] + " " + str(pf[1])
			else:
				sb += pf[0]
			od = " | "
		
		ad = " , "
	return sb
def parse_depends(line):
	print line

class CUDFPackage(object):

	def clone(self):
		nc = CUDFPackage()
		nc.name =self.name 
		nc.version =self.version 

		nc.recommends =self.recommends
		nc.depends =self.depends
		nc.conflicts =self.conflicts 
		nc.provides = self.provides 
		nc.arch = self.arch 
		nc.date =self.date 
		nc.priority =self.priority 
		nc.keep =self.keep 
		nc.installed =self.installed
		nc.properties =self.properties
		return nc
		
	def __init__(self, lines = [],basic=False) :
		self.name = ""
		self.version = -1

		self.recommends = []
		self.depends = []
		self.conflicts = []
		self.provides = []
		self.arch = ""
		self.date = -1
		self.priority = ""
		self.keep = ""
		self.installed = False
		self.properties = {}

		for line in lines:
			if line.strip() == "" :
				return
			elif line.startswith(_package):
				self.name = line[len(_package):].strip()
			elif line.startswith(_version):
				self.version = int(line[len(_version):].strip())
			elif line.startswith(_date):
				self.date = int(line[len(_date):].strip())
			elif not basic:
				if line.startswith(_depends):
					self.depends = parsePackageFormula(line[len(_depends):].strip())
				elif line.startswith(_provides):
					self.provides = parsePackageFormula(line[len(_provides):].strip())
				elif line.startswith(_recommends):
					self.recommends = parsePackageFormula(line[len(_recommends):].strip())
				elif line.startswith(_conflicts):
					self.conflicts = parsePackageFormula(line[len(_conflicts):].strip())
				elif line.startswith(_architecture):
					self.arch = line[len(_architecture):].strip()
			
				elif line.startswith(_installed):
					self.installed = line[len(_installed):].strip() == "true"
				elif line.startswith(_keep):
					self.keep = line[len(_keep):].strip()
				else:
					sp = line.split(":",1)
					self.properties[sp[0]] = sp[1].strip()

	def nv(self):
		return self.name,self.version

	def __str__(self):
		sb = ""
		el = "\n"
		sb += "package: " + self.name + el
		sb += "version: " + str(self.version) + el
		if(self.arch != ""):
			sb += "architecture: " +self.arch + el
		
		if(self.installed):
			sb += "installed: true" + el

		if(self.priority != ""):
			sb += "priority: " +self.priority + el

		if(self.keep != ""):
			sb += "keep: " +self.keep + el

		if(self.date != -1):
			sb += "date: " + str(self.date) +el

		if(self.depends != []):		
			sb += "depends: " + strPackageFormula(self.depends) + el
		if(self.conflicts != []):		
			sb += "conflicts: " + strPackageFormula(self.conflicts) + el
		if(self.provides != []):		
			sb += "provides: " + strPackageFormula(self.provides) + el
		if(self.recommends != []):		
			sb += "recommends: " + strPackageFormula(self.recommends) + el
		for prop in sorted(self.properties) :
			sb += prop + ": " + self.properties[prop] + el
		
		return sb




