#!/usr/bin/python

import pylab
import datetime,time
import shelve
from numpy import arange
import numpy

stratmap = [
		("Do Nothing", 			"DoNothing", 	 ""), 
		("APT w. Uptodate",		"ntd.APT",	'-removed,-new,-notuptodate'), #conservative
		#("Pro. APT w. Distance",	"Dist.APT.PRO",	'-uptodatedistance,-removed,-new'), #progressive
		#("Pro. Update Distance",	"Dist.Pro",	'-uptodatedistance,-changed'),
		("APT w. Distance",		"Dist.APT" ,	'-removed,-new,-uptodatedistance'),
		("Mixed Distance",		"Dist.MIXED",	'-removed,-uptodatedistance,-changed'),
		("Mixed Uptodate",		"ntd.MIXED",	'-removed,-notuptodate,-changed'),
		("Pro. APT w. Uptodate", 	"ntd.APT.PRO",	'-notuptodate,-removed,-new'),
		("Pro. Uptodate", 		"ntd.Pro",	'-notuptodate,-changed'),
		("Progressive Dist with Stable Constraints(28)", "Stable.Dist.Pro28",  '-stableversion(28),-uptodatedistance,-removed,-new'),
		("Conservative Dist with Stable Constraints(28)", "Stable.Dist.Con28",  '-stableversion(28),-removed,-new,-uptodatedistance'),
		("Progressive Dist with Stable Constraints(7)", "Stable.Dist.Pro7", '-stableversion(7),-uptodatedistance,-removed,-new'),
		("Conservative Dist with Stable Constraints(7)", "Stable.Dist.Con7", '-stableversion(7),-removed,-new,-uptodatedistance'),
		("Progressive Dist with Stable Constraints(14)", "Stable.Dist.Pro14", '-stableversion(14),-uptodatedistance,-removed,-new'),
		("Conservative Dist with Stable Constraints(14)", "Stable.Dist.Con14", '-stableversion(14),-removed,-new,-uptodatedistance'),
		("Product Distance New (1)", "Dist.Product.New", '-uptodatedistance.-new,-changed'),
		("Product Distance New (2)", "Product.Dist.New", '-new.-uptodatedistance,-changed'),
		("Trendy", "trendy", '-removed,-notuptodate,-unsat_recommends,-new')
	]
	
	
summary = {}
for name,strat,crit in stratmap:
	summary[strat] = shelve.open("." + strat + ".shelve")

#What are the most conservative and most progressive criteria
#We measure this by finding the one with the least change per user, and the one with the least distance per user

us = {}
for name,strat,crit in stratmap:
	if strat == "DoNothing":
		continue
	for user in sorted(summary[strat].keys()):
		if user == "user-00-04-00" or user == "user-00-05-00":
			continue
		if user not in us:
			us[user] = {}
		ham = summary[strat][user]["ham"]
		distm = summary[strat][user]["distmean"]
		size = summary[strat][user]["sizemean"]
		dist = summary[strat][user]["dist"]
		us[user][name] = (name,crit,sum(ham),numpy.mean(ham),numpy.std(ham),distm/size,numpy.std(dist))



for user in sorted(us.keys()):
	print "\\begin{table}[htp]"
	print "\\begin{tabular}{| l | c c |}\\hline"
	print " Criteria & Total change & Mean change & std. change & Mean uptodate distance & std Distance \\\\ \\hline"
	for name in sorted(us[user].keys(),key = lambda k : us[user][k][2]):
		p = us[user][name]
		bad = False
		for n, c, h, mh, sh, d,sd  in us[user].values():
			if h < p[2] and d < p[-1]:
				bad = True
		if bad:
			print "BAD %s (%s) & %.2f & %.2f &  %.2f &  %.3f &  %.3f\\\\" % p
		else:
			print "%s (%s) & %.2f & %.2f &  %.2f &  %.3f&  %.3f\\\\" % p
	print "\\end{tabular}"
	ns = ""
	if user == "user-00-01-00":
		ns = "daily"
	elif user == "user-00-02-00":
		ns = "weekly"
	elif user == "user-00-03-00":
		ns = "monthly"
	print "\\caption{ Update " + ns + ", sorted by Mean Change}"
	print "\\end{table}"
	print ""

		
#Conclusions:
#ntd.APT is the most conservative however it , Dist.APT has higher change becuase it can move to intermittant versions of a package
#Dist.APT.Pro and Dist.Pro are idetically progressive, but Dist.APT.Pro is significantly better performance
#if updating daily ntd.PRO and ntd.MIXED are WORSE THAN ntd.APT.PRO, so if you are progressive, dont use ntd
#ntd.MIXED == ntd.PRO and Dist.MIXED == Dist.PRO, this means that given no installations remove is never encountered.



#What effects does the use of progressive vs conservative criteria have on a system over a year?

#What is the difference of the hamming distance?
pylab.figure(1)
pylab.title("System change between critreia with weekly updates")
pylab.xticks(range(0,52,5))
width = .35
user = "user-00-02-00" #weekly
name,strat,crit = stratmap[1]
	
ham = summary[strat][user]["ham"]
pylab.bar(arange(len(ham)),ham,width,label=name)

name,strat,crit = stratmap[2]	
ham = summary[strat][user]["ham"]
pylab.bar(arange(width,len(ham)+width,1),ham,width,label=name)

pylab.xlabel("Weeks")
pylab.ylabel("System Change (Hamming distance)")
pylab.legend()

#What is the difference of the distance 
pylab.figure(2)
pylab.title("Uptodate Distance for critreia with daily updates")
pylab.xticks(range(0,365,20))

for name,strat,crit in stratmap[1:3] + stratmap[-8:]:
	user = "user-00-01-00" #daily
	dist = summary[strat][user]["dist"]
	pylab.plot(range(len(dist)),dist,label=name)

user = "user-00-00-00"
name,strat,crit = stratmap[0]
dist = summary[strat][user]["dist"]
pylab.plot(range(len(dist)),dist,label=name)


pylab.xlabel("Days")
pylab.ylabel("Uptodate Distance")
pylab.legend(loc=2)




	

