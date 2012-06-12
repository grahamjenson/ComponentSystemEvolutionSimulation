#!/usr/bin/python
import os
from cudfpy import cudfpkg
start = 1256814000 #one day before first action
end = 1288410600

initsys = cudfpkg.createProfileChangeRequest("9.10.cudf",nameversiononly=True)

def topnpairs(cudfmap):
	return set([(n,v) for n in cudfmap.keys() for v in cudfmap[n]])
	
	
def processSolutionsFolder(folder):
	assert os.path.isdir(folder)
	pcrl = {}
	for f in sorted(os.listdir(folder)):
		t = int(f.split(".")[0])
		
		ipcr = cudfpkg.createProfileChangeRequest(os.path.join(folder,f),nameversiononly=True)
		print folder,f,t
		pcrl[t] = ipcr
	return pcrl
	
	
def updatedPackages(ucudfs):
	
	cudfs = sorted(map(lambda x: (x[0],x[1].toMap()),ucudfs.items()),key=lambda x: x[0])
	deltas = zip(cudfs[:-1],cudfs[1:])
	def updated(((t1,prevcudf),(t2,newcudf))):
		pnv = topnpairs(prevcudf)
		nnv = topnpairs(newcudf)
		
		newpacks = nnv - pnv
		updatedpacks = []
		for p in newpacks:
			if p[0] in prevcudf.keys():
				updatedpacks.append(p[0])
		return updatedpacks
	
	return map(updated,deltas) 
	
	
cudfs = processSolutionsFolder("q1a/alwaysupdate.user.sols")
cudfs[start] = initsys

up = updatedPackages(cudfs)

groovy = {}

for l in range(1,60):
	groovy[l] = []
	for i, upacks in enumerate(up[:-l]):
		for p in upacks:
			for x in range(1,l+1):
				if p in up[i+x]:
					groovy[l].append((i,p))

import pylab
xy = []
for l in range(1,60):
	xy.append((l,len(groovy[l])))

x,y = zip(*sorted(xy))
pylab.plot(x,y)
pylab.scatter(x,y)
pylab.show()
