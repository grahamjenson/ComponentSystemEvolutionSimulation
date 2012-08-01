#!/usr/bin/python

import os 

userdirs = ["q5a"]
allusers = []
for ud in userdirs:
	purss = filter(lambda x : x.endswith("user"), map(lambda u : os.path.join(ud,u),os.listdir(ud)))
	purss = filter(lambda x : not os.path.exists(x+".sols"),purss)
	allusers = allusers + purss
	
print allusers

outfile = open("parrallelexecution.make",'w')
outfile.write("all: " + " ".join(map(str,range(len(allusers)))))
outfile.write("\n")
outfile.write("\n")
i = 0
for u in sorted(allusers):
	outfile.write(str(i) + ":\n")
	outfile.write("\tmkdir %s.sols;./gjsolver %s %s.sols 1> %s.out 2> %s.err \n\n" % tuple([u]*5))
	i += 1
#for u in q3/*.user; do echo $u >> q3.simlog;mkdir $u"sols";./gjsolver $u $u"sols" 1> $u.out 2> $u.err; done

