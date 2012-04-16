#!/usr/bin/python
import os
import shutil
import sys
import argparse
import random 
import time,datetime

__doc__ = """
g[enerate]Users is a script that takes variables in the use model, and generates a set of users to test
Usage: gUsers -d int -p int -i ifile -c cfile -u ufile
 -d - length in days 
 -p - number of users per permutation
 -i - install distribution file
 -c - weighted component file
 -u - update cycle file
 -t - total users

"""
	
def main():
	userFolder = "users"
	startdate = datetime.datetime(2009,10,31)
	
	parser = argparse.ArgumentParser(prog="generateCUDF", description='Generate a CUDF File')
	parser.add_argument('-p',"--preamble",  type=str, required=True)
	parser.add_argument('-u',"--userline",  type=str, required=True)
	parser.add_argument('-i',"--installFile",  type=str , required=True)
	parser.add_argument('-r',"--repFile",  type=str , required=True)
	parser.add_argument('-o',"--outFile",  type=str, required=True)
	
	args = parser.parse_args()
	if not os.path.exists(args.installFile):
		print "gCUDF: no install file"
		exit()
		
	if not os.path.exists(args.repFile):
		print "gCUDF: no repfile file"
		exit()	
		
	try:
		opts, args = getopt.getopt(sys.argv[1:], "hd:p:i:c:u:t:", [])
	except getopt.error, msg:
		print msg
		print "for help use -h"
		sys.exit(2)

	days = None
	iFile = None
	cFile = None
	uFile = None
	perms = None
	totalusers = -1
	# process options
	for o, a in opts:
		if o == ("-h"):
			print __doc__
			sys.exit(0)
		elif o == "-d":
			days = int(a)
		elif o == "-p":
			perms = int(a)
		elif o == "-i":
			iFile = a
		elif o == "-c":
			cFile = a
		elif o == "-u":
			uFile = a
		elif o == "-t":
			totalusers = int(a)
			
	#validate input
	error = False;
	if days == None:
		print "no days"
		error = True

	if perms == None:
		print "no permutations "
		error = True

	if iFile == None:
		print "no install distribution file"
		error = True
	elif not os.path.exists(iFile):
		print "no iFile"
		error = True

	if cFile == None:
		print "no weighted component file"
		error = True
	elif not os.path.exists(cFile):
		print "no cFile"
		error = True

	if uFile == None:
		print "no update cycle file"
		error = True
	elif not os.path.exists(uFile):
		print "no uFile"
		error = True
	
	if error : 
		sys.exit(1)


	
	
	#Process input
	#install distributions
	ilines = open(iFile).readlines()
	idists = []
	for line in ilines:
		line = line.strip()
		if line == "":
			continue;
		
		vals = line.split(",")
		vals = map(lambda x : float(x) ,vals)
		assert sum(vals) > 99 and sum(vals) < 101
		idists.append(map(lambda x: x/100.,vals))
	
	if totalusers != -1:
		idists = idists[:totalusers]
		
	#component weighted list
	clines = open(cFile).readlines()
	cweights = {}
	totalWeight = 0
	for line in clines:
		name,weight = line.split(",")
		assert weight != 0
		weight = float(weight)
		cweights[name] = weight	
		totalWeight += weight;

	cdists = map(lambda x : (x,1.0 * cweights[x]/totalWeight),cweights.keys())

	#update cycles
	udists = open(uFile).readlines()[0].split(",");
	udists = map(int,udists)

	

	#print "Install distributions ", idists
	#print "Update distributions ", udists
	#print "Weighted Components ", cdists 
	
	#print "Start date ", startdate
	
	#Create User profiles
	#step 1 generate intial user file
	if os.path.exists(userFolder):
		shutil.rmtree(userFolder)
	os.mkdir(userFolder)
	
	
	bname = "user-"
	for p in range(perms):
		ic = 0
		for idist in idists :
			uc = 0
			#make sure that the user installs the same thing across mutliple updates
			daylyinstalls = {}
			clist = list(cdists)
			for d in range(0,days):
				daylyinstalls[d] = []
				for i in range(select_from_weights(idist)):
						if(len(clist) == 0):
							print "Ran out of components"
							continue;
						comps, weights = zip(*clist)
						compindex = select_from_weights(weights)
						clist.remove(clist[compindex])
						daylyinstalls[d].append(comps[compindex])
			for udist in udists:
				#make the user file
				fn = bname+("%02d" % ic) + "-" + ("%02d" % uc) + "-" + ("%02d" % p)
				cuserFile = os.path.join(userFolder,fn)
				cuser = open(cuserFile,'w')
				
				for d in range(0,days):
					#time;installs;keep;update
					#what day is it
					date = startdate + datetime.timedelta(days=d)
					cuser.write(str(time.mktime(date.timetuple())))
					cuser.write(";")
					
					#what to install
					if len(daylyinstalls[d]) != 0:
						cuser.write("install:")
					delim = ""
					for i in daylyinstalls[d]:
						cuser.write(delim)
						delim = ","
						#what to install
						cuser.write(i)
						
					cuser.write(";")
					#what to keep
					
					keep = set()
					for k in range(d):
						keep = keep.union(daylyinstalls[k])
					if len(keep) != 0:
						cuser.write("keep:")
						
					delim = ""
					for k in keep:
						cuser.write(delim)
						delim = ","
						#what to install
						cuser.write(k)
						
					cuser.write(";")
					
					#Should update?
					if(udist != 0 and d % udist == 0) : 
						cuser.write("update")
						
					
						
					cuser.write("\n")
				uc += 1
			ic += 1

	#now all the users are generated we can actually limit them
	userfiles = os.listdir(userFolder)
	userfiles.sort()
	userfiles.reverse()
	ufs = {}
	for uf in userfiles:
		ufs[open(os.path.join(userFolder,uf),'r').read()] = uf
	
	for uf in userfiles:
		if uf not in ufs.values():
			os.remove(os.path.join(userFolder,uf))
	
def select_from_weights(weights):
	if len(weights) == 1: return 0;
	rnd = random.random() * sum(weights)
	for i, w in enumerate(weights):
		rnd -= w
		if rnd < 0:
			return i

if __name__ == "__main__":
	main()
