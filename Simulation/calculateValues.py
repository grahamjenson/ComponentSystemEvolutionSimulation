#!/usr/bin/python
from cudfpy import cudfpkg
import os
import shutil
import sys
import getopt
import subprocess
import time,datetime
import pickle
import networkx as nx
import numpy
import shelve
import gc 

__doc__ = """
Usage: gUsers 
 -i initial installation
 -s solutions folder
 -d days
"""



def notUpToDate(cudf,rep):
	i = 0
	for pkg in cudf.getInstalled():
		s = rep.getPackagesThatSatisfy((pkg.name,pkg.version,">"))
		if len(s) > 0 :
			i+=1
	return i

def upToDateDistance(cudf,rep):
	i = 0
	for pkg in cudf.getInstalled():
		s = rep.getPackagesThatSatisfy((pkg.name,pkg.version,">"))
		if len(s) > 0 :
			i+= len(s)
	return i

def hamming(cudf1, cudf2):
	set1 = set(map(cudfpkg.CUDFPackage.nv,cudf1.getInstalled()))
	set2 = set(map(cudfpkg.CUDFPackage.nv,cudf2.getInstalled()))
	return len(set1.symmetric_difference(set2))

def added(cudf1, cudf2):
	set1 = set(map(cudfpkg.CUDFPackage.nv,cudf1.getInstalled()))
	set2 = set(map(cudfpkg.CUDFPackage.nv,cudf2.getInstalled()))
	return len(set2 - set1)

def removed(cudf1, cudf2):
	set1 = set(map(cudfpkg.CUDFPackage.nv,cudf1.getInstalled()))
	set2 = set(map(cudfpkg.CUDFPackage.nv,cudf2.getInstalled()))
	return len(set1 - set2)

def same(cudf1, cudf2):
	set1 = set(map(cudfpkg.CUDFPackage.nv,cudf1.getInstalled()))
	set2 = set(map(cudfpkg.CUDFPackage.nv,cudf2.getInstalled()))
	return len(set1.intersection(set2))

def instability(rep,cudf):
	inst = {}
	for pack in cudf.getInstalled():
		p = rep.getPackagesThatSatisfy((pack.name,pack.version,"="))[0]
		inst[pack.nv()] = (int(p.properties["ce"]),int(p.properties["ca"]),int(p.properties["instability"]))
		
	return inst

def realinstability(G):
	inst = {}
	for pv in G.nodes():
			#Martin Metrics
			ec = 1.0 * len(set(G.out_edges(pv)))
			ac = 1.0 * len(set(G.in_edges(pv)))
			instability = 0
			if (ec+ac) != 0:	
				instability = ec/(ec+ac)
			inst[pv] = (int(ec), int(ac), int(instability*100))

	return inst
	
def pagerank(rep,cudf):
	pr = {}
	for pack in cudf.getInstalled():
		pr[pack.nv()] = (int(rep.getPackagesThatSatisfy((pack.name,pack.version,"="))[0].properties["pagerank"]))
		
	return pr

def realpagerank(G):
	rpr = {}
	pr = nx.pagerank(G)
	pr = zip(pr.keys(),pr.values())
	for pack,pagerank in pr:
		rpr[pack] = int(pagerank*1000000000000)

	return rpr

def hits(rep,cudf):
	hits = {}
	for pack in cudf.getInstalled():
		p = rep.getPackagesThatSatisfy((pack.name,pack.version,"="))[0]
		hits[pack.nv()] = (int(p.properties["hubs"]), int(p.properties["auth"]))
		
	return hits

def realhits(G):
	rhits = {}
	hubs , auths =nx.hits(G)
	for pack in hubs:
		rhits[pack] = (int(hubs[pack]*1000000000000),  int(auths[pack]*1000000000000))

	return rhits
		
		
def createResult(rep,psystem,csystem,t):
		result = {}
		
		currentCudf = getCUDF(csystem)
		prevCudf = getCUDF(psystem)
		
		result["uptodatedistace"] = upToDateDistance(currentCudf,rep)
		result["notUptodate"] = notUpToDate(currentCudf,rep)
		result["systemsize"] = len(currentCudf.getInstalled())
		
		
		#if the two systems are the same then there is no change in them
		result["dhamming"] = -1
		result["dadded"] = -1
		result["dremoved"] = -1
		result["dsame"] = -1
		result["time"] = -1
		
		if psystem != csystem: #then there was a change
			result["dhamming"] = hamming(prevCudf,currentCudf)
			result["dadded"] = added(prevCudf,currentCudf)
			result["dremoved"] = removed(prevCudf,currentCudf)
			result["dsame"] = same(prevCudf,currentCudf)
			result["time"] = t
		

		
		#CREATE GRAPH
		G = nx.DiGraph()
		instsystem = cudfpkg.ProfileChangeRequest()
		for pack in currentCudf.getInstalled():
			instsystem.add(rep.getPackagesThatSatisfy((pack.name,pack.version,"="))[0])
		
		#print "Creating Graph"
	
		result["percomponent"] = {}
		
		for pack in instsystem.getUniverse():
			G.add_node(pack.nv())
			result["percomponent"][pack.nv()] = {}
			for olist in pack.depends:
				ol = set()
				for pf in olist:
					ol = ol.union(instsystem.getPackagesThatSatisfy(pf))
				
				if len(ol) == 0:
					continue;
			
				for pdep in ol:
					G.add_edge(pack.nv(),pdep.nv())
				
		

		
		#print "Strongly Connected Components"
		
		result["scc"] = filter( lambda x : x > 1, map(len,nx.strongly_connected_components(G)))
		
		#Graph Properties
		
		#THRASH
		#print "density"
		
		result["density"] = nx.density(G)
		
		#print "cliques"
		
		result["cliques"] = nx.clique.graph_number_of_cliques(G)
		
		#print "shortest Path"
		
		#result["meanshortestpath"] = nx.algorithms.average_shortest_path_length(G)
		
		#print "load cent"
		
		#result["loaddist"] = nx.load_centrality(G)
		
		
		#print "average closeness"
		
		#result["meancloseness"] = numpy.mean(nx.closeness_centrality(G).values())
		
		
		
		
		
		
		#print "betweenness"
		bet = nx.betweenness_centrality(G)
		
		#print "instability"
		instab = instability(rep,currentCudf)
		
		rinstab = realinstability(G)
		
		#print "pagerank"	
		pr = pagerank(rep,currentCudf)
		
		rpr = realpagerank(G)
		
		#print "hits"
		hit = hits(rep,currentCudf)
		
		rhit = realhits(G)
		
		for pv in result["percomponent"].keys():
			result["percomponent"][pv]["betweeness"] = bet[pv]
			result["percomponent"][pv]["instability"] = instab[pv]
			result["percomponent"][pv]["realinstability"] = rinstab[pv]
			result["percomponent"][pv]["pagerankdist"] = pr[pv]
			result["percomponent"][pv]["realpagerankdist"] = rpr[pv]
			result["percomponent"][pv]["hitsdist"] = hit[pv]
			result["percomponent"][pv]["realhitsdist"] = rhit[pv]
		
		

		
		del rep
		del prevCudf
		del currentCudf
		del G
		del instsystem

		return result
		
def main():
	startdate = datetime.datetime(2009,10,31)
	
	try:
		opts, args = getopt.getopt(sys.argv[1:], "hi:s:d:", [])
	except getopt.error, msg:
		print msg
		print "for help use -h"
		sys.exit(2)

	initialCUDF = None
	solutionsFolder = None
	days = None
	
	# process options
	for o, a in opts:
		if o == ("-h"):
			print __doc__
			sys.exit(0)
		elif o == "-i":
			initialCUDF = a
		elif o == "-d":
			days = int(a)
		elif o == "-s":
			solutionsFolder = a

	#validate input
	error = False;
	if initialCUDF == None:
		print "no init cudf file"
		error = True
	elif not os.path.exists(initialCUDF):
		print "init cudf file doesnt exist"
		error = True

		
	if solutionsFolder == None:
		print "no sol folder cudf file"
		error = True
	elif not os.path.exists(solutionsFolder):
		print "sol folder doesnt exist"
		error = True
	
	if days == None:
		print "no days"
		error = True
	
			
	if error : 
		sys.exit(1)
	
	if solutionsFolder[-1] == "/":
		solutionsFolder = solutionsFolder[:-1]
	strategy = os.path.basename(solutionsFolder)
	
	reps = os.listdir("reps")
	reps.sort()
	reps.reverse()
	
	dates = {}
	
	#If you do days first it means that you dont continually read the repository CUDF
	repositories = {}
	for d in range(0,days):
		date = startdate + datetime.timedelta(days=d)
		mstime = time.mktime(date.timetuple())
		
		#identify repository for that day
		repository = None;
		for x in reps:
			if float(x) <= mstime:
				repository = x;
				break;
		
		repositories[date] = repository
		
		userstrat = {}
		dates[date] = userstrat

		for us in os.listdir(solutionsFolder):
			userstrat[us] = {}
			
			#we rely on the fact that the update cudf is lower then the install cudf in the sort DONT CHANGE THEIR NAMES
			systems = os.listdir(os.path.join(solutionsFolder,us))
			systems = filter(lambda x : x.endswith("cudf"),systems)
			systems.sort()
			systems.reverse()
			
			
			#current system, this takes the first system (in reverse order) less than or equal to the current date
			#If the system was installed, then it is after the update
			#MUST NOT BE A FAIL
			currentsystem = initialCUDF;
			currenttime = -1
			for x in systems:
				#Less than or equal to todays date
				if float(x.split(".")[0]) <= mstime:
					cs = os.path.join(solutionsFolder,us,x)
					if not open(cs).readline().startswith("FAIL"):
						currentsystem = cs;
						ctf = x+".time"
						timefile = open(os.path.join(solutionsFolder,us,ctf))
						line = timefile.readline().split()
						if len(line) < 1:
							print "ERROR AT",timefile
						line = line[1]
						#border case where the time file has a terminated line
						if line == "terminated":
							 line = timefile.readline().split()[1]
						currenttime = float(line)
						break;
					else:
						print "Fail system:",cs
					
			
			
			#previous system, this takes the first system (in reverse order) less than the current date
			#If the system was installed, then it is after the update
			#MUST NOT BE A FAIL
			previoussystem = initialCUDF
			for x in systems:
				#Less than or equal to todays date
				if float(x.split(".")[0]) < mstime:
					ps = os.path.join(solutionsFolder,us,x)
					if not open(ps).readline().startswith("FAIL"):
						previoussystem = ps;
						break;
					else:
						print "Fail system:",ps
			
			
			userstrat[us]["currentsystem"] = currentsystem
			userstrat[us]["previoussystem"] = previoussystem
			userstrat[us]["time"] = currenttime
			
	
	del reps
	c = 0
	
	
	mpath = "measurements"
	spath = os.path.join(mpath,strategy)
	if not os.path.exists(spath):
		os.mkdir(spath)
	
	for date in sorted(dates):
		c +=1
		tstr = str(int(time.mktime(date.timetuple()))) + ".pickle"
		
		todo = sorted(dates[date])
		#Remove any file already created
		todo = filter(lambda x : not os.path.exists(os.path.join(spath,x,tstr)),todo)
		if len(todo) == 0:
			print "Skipping: done all",date
			continue;
			
		print c,"/",len(dates),strategy,"Loading Repository : " + repositories[date]
		repo = cudfpkg.createProfileChangeRequest(os.path.join("reps",str(repositories[date])))
		print "Finsihed Loading Repository"
		c2 = 0
		for us in todo:
			c2 += 1
			print c,"/",len(dates), " ------ ",c2,"/",len(todo)
			
			
			t = dates[date][us]["time"]

			res = createResult(repo,dates[date][us]["previoussystem"],dates[date][us]["currentsystem"],t)
			
			sys.stdout.write("saving ")
			
			if not os.path.exists(os.path.join(spath,us)):
				os.mkdir(os.path.join(spath,us))
			pickle.dump(res,open(os.path.join(spath,us,tstr),'w'))
			
			sys.stdout.write("saved\n")
		del repo
		gc.collect()
	

def getCUDF(cfile):
	return cudfpkg.createProfileChangeRequest(cfile)
	


if __name__ == "__main__":
	main()
