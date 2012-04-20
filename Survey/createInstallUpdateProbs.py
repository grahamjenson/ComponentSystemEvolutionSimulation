#!/usr/bin/python

import pickle
import datetime
import os
amdir = "actionmaps"
out = open("probs",'w')
out.write("#update , install \n")
for useram in filter(lambda x : x.endswith("map"),os.listdir(amdir)):
	user = pickle.load(open(os.path.join(amdir,useram)))
	installs = {}
	upgrades = {}

	
	for d in  user.keys():
		up = user[d]['upgrade']
		ins = user[d]['install']
		if up > 0 and ins == 0:
			if d.date() not in upgrades.keys():
				upgrades[d.date()] = 0
			upgrades[d.date()] = upgrades[d.date()] +1
		if ins > 0:
			if d.date() not in installs.keys():
				installs[d.date()] = 0
			installs[d.date()] = installs[d.date()] +1
	

	tu =len(installs.keys())
	ti =len(upgrades.keys())
	
	
	
	mindate = min(user.keys())
	maxdate = max(user.keys())	
	totaldays = (maxdate-mindate).days
	
	print tu,ti,totaldays
	
	out.write("%s , %f , %f \n" % (useram,(1.0*tu)/totaldays,(1.0*ti)/totaldays))
	
out.close()
