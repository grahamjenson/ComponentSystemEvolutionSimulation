#!/usr/bin/python
import os
import pylab
import shelve
from matplotlib import dates
import numpy
from matplotlib.colors import ColorConverter
import datetime
from analysisutils import *

from matplotlib.ticker import MaxNLocator


folder = "cache/q3"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))

hi = filter(lambda x : os.path.basename(x).startswith("highinstall"),files);
hu = filter(lambda x : os.path.basename(x).startswith("highupdate"),files);
lc = filter(lambda x : os.path.basename(x).startswith("lowchange"),files);
mc = filter(lambda x : os.path.basename(x).startswith("mediumchange"),files);

folder = "cache/q6"
files = map(lambda x : os.path.join(folder,x),os.listdir(folder))

chi = filter(lambda x : os.path.basename(x).startswith("conservativehighinstall"),files);
chu = filter(lambda x : os.path.basename(x).startswith("conservativehighupdate"),files);
clc = filter(lambda x : os.path.basename(x).startswith("conservativelowchange"),files);
cmc = filter(lambda x : os.path.basename(x).startswith("conservativemediumchange"),files);

phi = filter(lambda x : os.path.basename(x).startswith("progressivehighinstall"),files);
phu = filter(lambda x : os.path.basename(x).startswith("progressivehighupdate"),files);
plc = filter(lambda x : os.path.basename(x).startswith("progressivelowchange"),files);
pmc = filter(lambda x : os.path.basename(x).startswith("progressivemediumchange"),files);

norms = [("HI", hi, "#FF0000"), 			("MC", mc, "#0000FF"), 			("HU", hu, "#00FF00"), 			("LC", lc, "#FF00FF")]
cons = [("Con. Upgrade HI", chi, "#FF0000"), 		("Con. Upgrade MC", cmc, "#0000FF"), 	("Con. Upgrade HU", chu, "#00FF00"), 	("Con. Upgrade LC", clc, "#FF00FF")]
pros = [("Pro. Upgrade HI", phi, "#FF0000"), 		("Pro. Upgrade MC", pmc, "#0000FF"),	("Pro. Upgrade HU", phu, "#00FF00"),  	("Pro. Upgrade LC", plc, "#FF00FF")]

his = [("Upgrade HI", hi, "#FF0000"),("Con. Upgrade HI", chi, "#00FF00"),("Pro. Upgrade HI", phi, "#0000FF")]
hus = [("Upgrade HU", hu, "#FF0000"),("Con. Upgrade HU", chu, "#00FF00"),("Pro. Upgrade HU", phu, "#0000FF")]
mcs = [("Upgrade MC", mc, "#FF0000"),("Con. Upgrade MC", cmc, "#00FF00"),("Pro. Upgrade MC", pmc, "#0000FF")]
lcs = [("Upgrade LC", lc, "#FF0000"),("Con. Upgrade LC", clc, "#00FF00"),("Pro. Upgrade LC", plc, "#0000FF")]


# High install 	0.42159     0.7638915 
# Medium change 0.22202257  0.25948714
# High update 	0.5610895   0.1918915 
# Low change  	0.18629183  0.08623483





def plotuttdpc():
	pylab.figure(1)
	
	pylab.subplot(311)
	
	for name,pf,c in norms: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in norms: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "%s, mean uttdpc %f, final val %f" % (name,mdiff,imean[-1])
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.title("Uptodate Distance per Component of Users")
	pylab.ylim([0,1])
	
	pylab.subplot(312)
	
	for name,pf,c in cons: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in cons: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "%s, mean uttdpc %f, final val %f" % (name,mdiff,imean[-1])
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.ylabel("Uptodate Distance per Component")
	pylab.title("Uptodate Distance per Component of \"Conservative\" Users")
	pylab.ylim([0,1])
	
	pylab.subplot(313)
	
	for name,pf,c in pros: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in pros: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "%s, mean uttdpc %f, final val %f" % (name,mdiff,imean[-1])
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.title("Uptodate Distance per Component of \"Progressive\" Users")
	pylab.ylim([0,1])
	
	
	saveFigure("q6usersuttd",size=(13,20))
	
	
	return 
	#-------------------------------------------------


	pylab.figure(4)
	
	for name,pf,c in his: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in his: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean uttdpc",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Uptodate Distance per Component")
	pylab.title("Uptodate Distance per Component of \"HI\" Users")
	
	saveFigure("q6hiusersuttd")
	
	
	pylab.figure(5)
	
	for name,pf,c in hus: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in hus: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean uttdpc",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Uptodate Distance per Component")
	pylab.title("Uptodate Distance per Component of \"HU\" Users")
	
	saveFigure("q6huusersuttd")
	
	pylab.figure(6)
	
	for name,pf,c in mcs: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in mcs: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean uttdpc",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Uptodate Distance per Component")
	pylab.title("Uptodate Distance per Component of \"MC\" Users")
	
	saveFigure("q6mcusersuttd")
	
	pylab.figure(7)
	
	for name,pf,c in lcs: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in lcs: 
		ivals = map(lambda x : uttdperc(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean uttdpc",name,mdiff

		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) UTTDpC of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Uptodate Distance per Component")
	pylab.title("Uptodate Distance per Component of \"LC\" Users")
	
	saveFigure("q6lcusersuttd")
	
	
def plotchange():
	pylab.figure(11)
	pylab.subplot(311)
	
	for name,pf,c in norms: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in norms: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean Change",name,mdiff
		print "Final Change, %s (mean : %f , std %f)  7days (mean : %f , std %f)" % (name, imean[-1],istd[-1], imean[-7],istd[-7])
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 50 \"%s\" users" % name))
	
	#Final Change, HI (mean : 3176 , std 196)  7days (mean : 3136 , std 186)
	#Final Change, HU (mean : 2123 , std 135)  7days (mean : 2108 , std 135)
	#Final Change, MC (mean : 2291 , std 164)  7days (mean : 2271 , std 164)
	#Final Change, LC (mean : 1903 , std 121)  7days (mean : 1885 , std 120)
	
	#Final Change, Con. Upgrade HI (mean : 3089 , std 159)  7days (mean : 3036 , std 164)
	#Final Change, Con. Upgrade HU (mean : 2091 , std 158)  7days (mean : 2065 , std 161)
	#Final Change, Con. Upgrade MC (mean : 2267 , std 153)  7days (mean : 2231 , std 149)
	#Final Change, Con. Upgrade LC (mean : 1822 , std 96)  7days (mean : 1797 , std 98)
	
	#HI = 3136 - 3089 = 47
	#HU = 2108 - 2091 = 17
	#MC = 2271 - 2267 = 4
	#LC = 1885 - 1822 = 63
	
	#HI, mean uttdpc 0.364631, final val 0.797584
	#HU, mean uttdpc 0.316690, final val 0.826326
	#MC, mean uttdpc 0.338309, final val 0.795248
	#LC, mean uttdpc 0.307916, final val 0.772981
	
	#Con. Upgrade HI, mean uttdpc 0.381826, final val 0.794031
	#Con. Upgrade HU, mean uttdpc 0.343298, final val 0.816037
	#Con. Upgrade MC, mean uttdpc 0.351951, final val 0.780787
	#Con. Upgrade LC, mean uttdpc 0.338918, final val 0.820539
	
	#Pro. Upgrade HI, mean uttdpc 0.187131, final val 0.432870
	#Pro. Upgrade HU, mean uttdpc 0.191693, final val 0.418295
	#Pro. Upgrade MC, mean uttdpc 0.244713, final val 0.632974
	#Pro. Upgrade LC, mean uttdpc 0.257741, final val 0.620028

	
	pylab.legend(loc="upper left")
	pylab.title("Total Change of Users")
	pylab.ylim([0,4000])
	
	pylab.subplot(312)
	
	for name,pf,c in cons: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in cons: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean Change",name,mdiff
		print "Final Change, %s (mean : %f , std %f)  7days (mean : %f , std %f)" % (name, imean[-1],istd[-1], imean[-7],istd[-7])
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.ylabel("Total Change")
	pylab.title("Total Change of \"Conservative\" Users")
	pylab.ylim([0,4000])
	
	pylab.subplot(313)
	
	for name,pf,c in pros: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in pros: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean Change",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.title("Total Change of \"Progressive\" Users")
	pylab.ylim([0,4000])
	
	saveFigure("q6userchange",size=(13,20))
	
	
	return 
	#----------------------------------------
	
	
	pylab.figure(14)
	
	for name,pf,c in his: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in his: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean Change",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Total Change")
	pylab.title("Total Change of \"HI\" Users")
	
	saveFigure("q6hiuserchange")
	
	
	pylab.figure(15)
	
	for name,pf,c in hus: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in hus: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean Change",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Total Change")
	pylab.title("Total Change of \"HU\" Users")
	
	saveFigure("q6huuserchange")
	
	pylab.figure(16)
	
	for name,pf,c in mcs: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in mcs: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean Change",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Total Change")
	pylab.title("Total Change of \"MC\" Users")
	
	saveFigure("q6mcuserchange")
	
	pylab.figure(17)
	
	for name,pf,c in lcs: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		pylab.fill_between(pallthedays, imeanpstd, imeanmstd, facecolor=c, alpha=0.2)
	
	for name,pf,c in lcs: 
		ivals = map(lambda x : chtt(x),pf)
		imean,istd,imeanpstd,imeanmstd = multimeanstd(ivals)
		mdiff = numpy.mean(imean)
		print "Mean Change",name,mdiff
		pylab.plot(pallthedays,imean,color=c,label=("Mean (+-1std) Total Change of 50 \"%s\" users" % name))

	pylab.legend(loc="upper left")
	pylab.xlabel("Date")
	pylab.ylabel("Total Change")
	pylab.title("Total Change of \"LC\" Users")
	
	saveFigure("q6lcuserchange")
	

plotuttdpc()	
plotchange()


