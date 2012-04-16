#!/usr/bin/python
import pickle
import shelve
import os
import numpy

strats = ["DoNothing",
"Dist.APT","Dist.APT.PRO","Dist.Pro","ntd.APT","ntd.APT.PRO","ntd.Pro","Dist.MIXED",
"ntd.MIXED","ntd.APTl","Dist.APT.PROl",
"Stable.Dist.Pro7","Stable.Dist.Con7",
"Stable.Dist.Pro14","Stable.Dist.Con14",
"Stable.Dist.Pro28","Stable.Dist.Con28",
"trendy","Dist.Product.New","Product.Dist.New"]

mpath = "measurements"
days = 365.



for s in strats:
	spath = os.path.join(mpath,s)
	summary = {}
	
	res = shelve.open("." + s +".shelve")
		
	for user in sorted(os.listdir(spath)):
		
		if user in res.keys():
			print "skip",user,s
			continue
			
		print s,user
		summary[user] = {}
		upath = os.path.join(spath,user);
		
		tdist = []
		tsize = []
		tntd = []
		ttime = []
		tdens = []
		tcliq = []
		
		tnscc = []
		tmscc = []
		
		tham = []
		tadd = []
		trem = []
		tsame = []
		
		ddist = []
		comps = set()
		
		fs = sorted(os.listdir(upath))
		vx = 0
		for d in fs:
			vx +=1
			print s,user,vx,"/",len(fs)
			p = pickle.load(open(os.path.join(upath,d)))
			
			

			
			tdist.append(p["uptodatedistace"])
			
			tsize.append(p["systemsize"])
			tntd.append(p["notUptodate"])
			tdens.append(p["density"])
			tcliq.append(p["cliques"])
			
			scc = p["scc"]
			tnscc.append(len(scc)) 
			tmscc.append(numpy.mean(scc))
			
			#because of earlier error we assume that there is always a change
			
			ham = p["dhamming"]
			if ham > 0:
				tham.append(ham)
				if len(tdist) > 1:
					ddist.append(tdist[-2] - tdist[-1])
				
			time = int(p["time"])
			if ham > 0:
				ttime.append(time)
			
			
			
				
			added = p["dadded"]
			if ham > 0:
				tadd.append(added)
					
			removed = p["dremoved"]
			if ham > 0:
				trem.append(removed)
				
			same = p["dsame"]
			if ham > 0:
				tsame.append(same)
			
			comps = comps.union(set(p["percomponent"].keys()))
			
			
		summary[user]["ham"] = tham
		summary[user]["hampd"] = sum(tham)/days
		summary[user]["hammean"] = numpy.mean(tham)
		summary[user]["hamstd"] = numpy.std(tham)
		
		summary[user]["addpd"] = sum(tadd)/days
		summary[user]["addmean"] = numpy.mean(tadd)
		summary[user]["addstd"] = numpy.std(tadd)
		
		summary[user]["rempd"] = sum(trem)/days
		summary[user]["remmean"] = numpy.mean(trem)
		summary[user]["remstd"] = numpy.std(trem)
		
		summary[user]["samepd"] = sum(tsame)/days
		summary[user]["samemean"] = numpy.mean(tsame)
		summary[user]["samestd"] = numpy.std(tsame)
		
		summary[user]["deltadist"] = ddist
		summary[user]["deltadistmean"] = numpy.mean(ddist)
		summary[user]["deltadiststd"] = numpy.std(ddist)
		
		summary[user]["dist"] = tdist
		summary[user]["distmean"] = numpy.mean(tdist)
		summary[user]["diststd"] = numpy.std(tdist)
		
		
		summary[user]["timemean"] = numpy.mean(ttime)
		summary[user]["timestd"] = numpy.std(ttime)
		
		
		summary[user]["sizemean"] = numpy.mean(tsize)
		summary[user]["sizestd"] = numpy.std(tsize)
		
		
		summary[user]["ntdmean"] = numpy.mean(tntd)
		summary[user]["ntdstd"] = numpy.std(tntd)
		
		
		summary[user]["densmean"] = numpy.mean(tdens)
		summary[user]["densstd"] = numpy.std(tdens)
		
		
		summary[user]["cliqmean"] = numpy.mean(tcliq)
		summary[user]["cliqstd"] = numpy.std(tcliq)
		
		
		summary[user]["nsccmean"] = numpy.mean(tnscc)
		summary[user]["nsccstd"] = numpy.std(tnscc)
		
		
		summary[user]["msccmean"] = numpy.mean(tmscc)
		summary[user]["msccstd"] = numpy.std(tmscc)
		
		summary[user]["components"] = comps
		
	print "syncing"
	for u in summary.keys():
		
		res[u] = summary[u]
		
	res.close()
	
