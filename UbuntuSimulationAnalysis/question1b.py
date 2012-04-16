#!/usr/bin/python

import pylab
import datetime,time
import shelve


stratmap = [
		("Do Nothing", 			"DoNothing", ""), 
		("APT w. Uptodate",	"ntd.APT",	'-removed,-new,-notuptodate'), #conservative
		("Pro. APT w. Distance",	"Dist.APT.PRO",	'-uptodatedistance,-removed,-new'), #progressive
		("Pro. Update Distance",	"Dist.Pro",	'-uptodatedistance,-changed'),
		("APT w. Distance",	"Dist.APT" ,	'-removed,-new,-uptodatedistance'),
		
		("Pro. APT w. Uptodate", 	"ntd.APT.PRO",	'-notuptodate,-removed,-new'),
		("Pro. Uptodate", 	"ntd.Pro",	'-notuptodate,-changed'),
	]
	
	
startdate = datetime.datetime(2009,10,31)

summary = {}
for name,strat,crit in stratmap:
	summary[strat] = shelve.open("." + strat + ".shelve")

dates = map(lambda x : int(x.split()[1]), open("allpacks").readlines()[1::3])

releases = [datetime.datetime(2009,4,23),datetime.datetime(2009,10,29),datetime.datetime(2010,4,29),datetime.datetime(2010,10,10)]
releasetimes = map(lambda x : time.mktime(x.timetuple()),releases)
names = ["Ubuntu 9.04 (Jaunty Jackalope)","Ubuntu 9.10 (Karmic Koala)","Ubuntu 10.04 LTS (Lucid Lynx)","Ubuntu 10.10 (Maverick Meerkat)"]
td = datetime.timedelta(days = 56)
releasecycles = map(lambda x : (x-td,x,x+td),releases)

pylab.figure(1)

fig = 0
for sd,r,ed in releasecycles:
	fig +=1
	ax = pylab.subplot(220 + fig)
	st = time.mktime(sd.timetuple())
	rr = time.mktime(r.timetuple())
	et = time.mktime(ed.timetuple())
	ax.set_title(names[fig-1] + " release")
	
	rt = filter(lambda x: x > st and x < et, dates)
	rt = map(lambda x: (x - rr)/(60*60*24), rt) 
	ax.hist(rt,16)
	ax.set_xticks(range(-56,57,14))
	ax.set_yticks(range(0,3001,500))
	ax.axvline(0,color='r',label="release")
	ax.text(1,2825,"release",color='r')
	ax.set_xlabel("Days from release")
	ax.set_ylabel("# of pacakages added to repository")
	#ax.legend()
	



def updatecyclecomparisontable(strats):

	stratline = " "
	critline = " "
	userline  = " "
	ddist1 = "Change in Distance 9.10"
	ddist2 = "Change in Distance 10.04"
	dham1 = "Change 9.10"
	dham2 = "Change 10.04"


	
	for name,strat,crit in strats:

		user = "user-00-04-00"
		if user not in summary[strat].keys():
			continue
		stratline += "& \multicolumn{2}{|c|}{"+ name + "}"
		critline +=  "& \multicolumn{2}{|c|}{"+ crit + "}"
	
		userline += "& on release"
		d = summary[strat][user]["dist"]
		dd = [x - d[i+1] for i,x in enumerate(d[:-1]) if x > d[i+1]]
		ddist1 += "& " + str(dd[0])
		ddist2 += "& " + str(dd[1])
		dham = summary[strat][user]["ham"]
		dham1 += "& " + str(dham[0])
		dham2 += "& " + str(dham[1])
	
		
	
		 
		user = "user-00-05-00"
		userline += "& 2 weeks" 
		d = summary[strat][user]["dist"]
		dd = [x - d[i+1] for i,x in enumerate(d[:-1]) if x > d[i+1]]
		ddist1 += "& " + str(dd[0])
		ddist2 += "& " + str(dd[1])
		dham = summary[strat][user]["ham"]
		dham1 += "& " + str(dham[0])
		dham2 += "& " + str(dham[1])
	
		

	print "Comparison between updating on the release and updating 2 weeks after release"

	print "\\begin{table}[htp]"
	print "\\begin{tabular}{| l " + ("| c c |" * len(strats)) + "}\\hline"

	print stratline + "\\\\"
	print critline + "\\\\ \\hline"
	print userline + "\\\\"
	print ddist1  + "\\\\"
	print ddist2 + "\\\\"
	print dham1  + "\\\\"
	print dham2  + "\\\\"
	print "\\end{tabular}"
	print "\\end{table}"
	print ""

updatecyclecomparisontable(stratmap[0::3])
updatecyclecomparisontable(stratmap[1::3])
updatecyclecomparisontable(stratmap[2::3])

pylab.figure(2)
ax = pylab.subplot(1,1,1)
for name,strat,crit in stratmap:

		user = "user-00-04-00"
		if user not in summary[strat].keys():
			continue
			
		
		d = summary[strat][user]["dist"]
		ax.plot(range(len(d)),d,color='r',label="Update on release")
		print strat, user, len(d)
		
		user = "user-00-05-00"
		d = summary[strat][user]["dist"]
		ax.plot(range(len(d)),d,color='b',label="Update after release")

		print strat, user, len(d)
#only print legend of first two
handles, labels = ax.get_legend_handles_labels()
ax.legend(handles[:2],labels[:2],loc=2)

pylab.figure(3)
pylab.title("Uptodate Distance for a system")
pylab.xticks(range(0,365,20))

user = "user-00-00-00"
name,strat,crit = stratmap[0]
dist = summary[strat][user]["dist"]
pylab.plot(range(len(dist)),dist,label=name)

startdate = datetime.datetime(2009,10,31)
diff = map(lambda x : (x - startdate).days, releases[2:4])
for d in diff:
	pylab.axvline(d)

pylab.xlabel("Days")
pylab.ylabel("Uptodate Distance")
pylab.legend(loc=2)

pylab.show()

