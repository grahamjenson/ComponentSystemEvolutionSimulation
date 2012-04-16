#!/usr/bin/python
from cudfpy import cudfpkg
import os
import shutil
import sys
import getopt
import networkx as nx
import datetime, time
__doc__ = """
addProperties script will add properties hardcoded into a cudf file, to be used when selecting packages to install.
c -- cudf file
p -- properties
properties = pagerank|hits|graph

graph= martin and Jens

martin = Afferent coupling, efferent coupling, instability
jens = meta component 
"""

puprop = "puweight"
		
def main():
	try:
		opts, args = getopt.getopt(sys.argv[1:], "hc:p:", [])
	except getopt.error, msg:
		print msg
		print "for help use -h"
		sys.exit(2)

	cudfFile = None
	properties = []
	print time.ctime()
	# process options
	for o, a in opts:
		if o == ("-h"):
			print __doc__
			sys.exit(0)
		elif o == "-c":
			cudfFile = a
		elif o == "-p":
			properties = a.split(",")

	#validate input
	error = False;

	if cudfFile == None:
		print "no cudfFile line"
		error = True
	elif not os.path.exists(cudfFile):
		print "cudf file doesnt exist"
		error = True
	
	if len(properties) ==0:
		print "no properties?"
		error = True
		
	if error : 
		sys.exit(1)

	print cudfFile
	
	#The new PCR, based off of the repository
	print "Creating CUDF"
	cudf = cudfpkg.createProfileChangeRequest(cudfFile)
	
	
	G = None
	if "pagerank" in properties or "hits" in properties or "graph" in properties:
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
				
	if "age" in properties:
		print "Adding Age"
		repdate = datetime.datetime.fromtimestamp(int(float(os.path.basename(cudfFile))))
		for pack in cudf.getUniverse():
				age = (repdate - datetime.datetime.fromtimestamp(pack.date)).days
				pack.properties["age"] =  str(age)
				
	if "pagerank" in properties:
		print "Calculating Page Rank"
		try:
			pr = nx.pagerank(G)
			pr = zip(pr.keys(),pr.values())
			for pack,pagerank in pr:
				pack.properties["pagerank"] = str(int(pagerank*1000000000000))
			
		except nx.NetworkXError:
			print "Pagerank failed"
			
		
	
	if "hits" in properties:
		print "Calculating HITS"
		
		try:
			hubs , auths =nx.hits(G)
			for pack in hubs:
				pack.properties["hubs"] = str(int(hubs[pack]*1000000000000))
				pack.properties["auth"] = str(int(auths[pack]*1000000000000))
			
		except nx.NetworkXError:
			print "HITS failed"
			

	
	
			
	if "graph" in properties:
		print "Adding Graph Properties"
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
				
				pv.properties["metains"] = str(mi)
				pv.properties["metaouts"] = str(mo)
				pv.properties["metaversion"] = str(mv)
				
				#Martin Metrics
				ec = 1.0 * len(set(G.out_edges(pv)))
				ac = 1.0 * len(set(G.in_edges(pv)))
				instability = 0
				if (ec+ac) != 0:	
					instability = ec/(ec+ac)
				pv.properties["ce"] = str(int(ec))
				pv.properties["ca"] = str(int(ac))
				pv.properties["instability"] = str(int(instability*100))
				
	f = open(cudfFile,'w')
	f.write(str(cudf))
	f.close()
	
if __name__ == "__main__":
	main()
