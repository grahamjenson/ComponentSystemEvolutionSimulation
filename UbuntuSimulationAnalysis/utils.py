from cudfpy import cudfpkg
import os
import networkx as nx
import datetime, time



########CREATING THE CUDFS############

def processSolutionsFolder(folder):
	assert os.path.isdir(folder)
	pcrl = {}
	for f in sorted(os.listdir(folder)):
		t = int(f.split(".")[0])
		
		ipcr = cudfpkg.createProfileChangeRequest(os.path.join(folder,f),nameversiononly=True)
		print folder,f,t
		pcrl[t] = ipcr
	return pcrl
		
def sliceCUDF(t,cudf):
	npcr = cudfpkg.ProfileChangeRequest()
	for p in cudf.getUniverse():
		if p.date <= t:
			npcr.add(p)
	return npcr

def createFullCUDF(icudf, sliceCUDF):
	npcr = cudfpkg.ProfileChangeRequest()
	for p in sliceCUDF.getUniverse():
		iil = icudf.getPackagesThatSatisfy((p.name,p.version,"="))
		if len(iil) > 0 :
			#Only clone those that change
			np = p.clone()
			np.installed = True
			npcr.add(np)
		else:
			npcr.add(p)
	return npcr
		


############METRICS####################	

			
def createGraph(cudf):
	G = nx.DiGraph()

	print "Creating Graph"

	for pack in cudf.getUniverse():
		G.add_node(pack)
		for olist in pack.depends:
			ol = set()
			for pf in olist:
				ol = ol.union(cudf.getPackagesThatSatisfy(pf))
		
			if len(ol) == 0:
				continue;
	
	
			for pdep in ol:
				G.add_edge(pack,pdep)
	

def getPageRank(G):		
	print "Calculating Page Rank"
	ret = []
	try:
		pr = nx.pagerank(G)
		ret = pr.items()
	except nx.NetworkXError:
		print "Pagerank failed"
	
	return ret
		
	
def getHits(G):
	print "Calculating HITS"
	ret = []
	try:
		hubs , auths =nx.hits(G)
		for pack in hubs:
			ret.append((pack,(hubs[pack],auth[pack])))
		
	except nx.NetworkXError:
		print "HITS failed"
	return ret 



def getMartin(G,cudf):		
	rt = []
	print "Calculating Martin Metrics"
	for pn in cudf.getPackageNames():
		mi = 0
		mo = 0
		mv = 0
		
		ins = None
		outs = None
		for pv in sorted(cudf.getPackagesThatSatisfy((pn,-1,"")), key=lambda x : x.version):
			#Jens MetaVersion Metrics
			
			po = set(G.out_edges(pv))
			pi = set(G.in_edges(pv))
			increaseMetaVersion = False
			
			if ins != pi:
				mi += 1
				increaseMetaVersion = True
				
			if outs != po:
				mo += 1
				increaseMetaVersion = True
			
			if increaseMetaVersion:
				mv += 1
			
			ins = pi
			outs = po
			
			#Martin Metrics
			ec = 1.0 * len(set(G.out_edges(pv)))
			ac = 1.0 * len(set(G.in_edges(pv)))
			instability = 0
			if (ec+ac) != 0:	
				instability = ec/(ec+ac)
			rt.append((pv,(int(ec),int(ac), instability)))
	return rt



def getUptoDateDistance(start,end,cudfs,uttddict):
	uttd = []
	acudfs = getCudfs(start,end,cudfs)
	for t,icudf in acudfs:
		total = 0
		for package in icudf.getUniverse():
			allversions = uttddict[package.name].items()
			#print package.nv(), "1", allversions
			allversions = filter(lambda x: x[1] <= t,allversions)
			#print package.nv(), "2", t,allversions
			allversions = sorted(allversions,key = lambda x : -x[0])
			#print package.nv(), "3", allversions
			allversions = map(lambda x : x[0], allversions)
			#print package.nv(), "4", allversions
			mv = allversions.index(package.version)
			#print package.nv(), "5", mv
			
			total += mv
		uttd.append((t,total))
	return uttd
	
def topnpairs(cudfmap):
	return set([(n,v) for n in cudfmap.keys() for v in cudfmap[n]])
	
def newNames(ucudfs):
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	deltas = zip(cudfs[:-1],cudfs[1:])
	def new(((t1,prevcudf),(t2,newcudf))):
		pnv = set(prevcudf.keys())
		nnv = set(newcudf.keys())
		return t2,len(nnv - pnv)
	return map(new,deltas)

def removedNames(ucudfs):
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	deltas = zip(cudfs[:-1],cudfs[1:])
	def removed(((t1,prevcudf),(t2,newcudf))):
		pnv = set(prevcudf.keys())
		nnv = set(newcudf.keys())
		return t2,len(pnv - nnv)
	
	return map(removed,deltas)

def changedNames(ucudfs):
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	deltas = zip(cudfs[:-1],cudfs[1:])
	def changed(((t1,prevcudf),(t2,newcudf))):
		pnv = set(prevcudf.keys())
		nnv = set(newcudf.keys())
		tot = 0;
		for n in pnv.union(nnv):
			if (n in prevcudf) and (n in newcudf):
				if sorted(prevcudf[n]) != sorted(newcudf[n]):
					tot +=1
			else:
				tot += 1
			
		return t2, tot
	
	return map(changed,deltas)

def removedPackages(ucudfs):
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	deltas = zip(cudfs[:-1],cudfs[1:])
	def removed(((t1,prevcudf),(t2,newcudf))):
		pnv = topnpairs(prevcudf)
		nnv = topnpairs(newcudf)
		
		return t2,len(pnv - nnv)
	
	return map(removed,deltas)

def size(ucudfs):
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	def size((t,cudf)):
		pnv = topnpairs(cudf)
		
		return t,len(pnv)
	
	return map(size,cudfs)
	
def newPackages(ucudfs):
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	deltas = zip(cudfs[:-1],cudfs[1:])
	def new(((t1,prevcudf),(t2,newcudf))):
		pnv = topnpairs(prevcudf)
		nnv = topnpairs(newcudf)
		
		return t2,len(nnv - pnv)
	
	return map(new,deltas)


def updatedPackages(start,end,ucudfs):
	
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	deltas = zip(cudfs[:-1],cudfs[1:])
	def updated(((t1,prevcudf),(t2,newcudf))):
		pnv = topnpairs(prevcudf)
		nnv = topnpairs(newcudf)
		
		newpacks = nnv - pnv
		updatedpacks = []
		for p in newpacks:
			if p[0] in prevcudf.keys():
				updatedpacks.append(p)
		return t2,len(updatedpacks)
	
	return map(updated,deltas) 

def getCudfs(start,end,cudfs):
	ncudfs = []
	for t in range(start,end,24*60*60):
		ncudfs.append((t,getCudf(cudfs,t)))
	return ncudfs

def getCudf(cudfs,t):
	sortedsystems = sorted(cudfs.items(),key=lambda x : -x[0])
	for c in sortedsystems:
		if c[0] <= t:
			return c[1]	
