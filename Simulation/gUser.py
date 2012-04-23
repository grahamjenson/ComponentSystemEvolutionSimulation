#!/usr/bin/python
import os
import shutil
import sys
import argparse
import random 
import time,datetime


#initial system
#time, request, system, 
	
def main():
	userFolder = "users"
	
	
	parser = argparse.ArgumentParser(prog="generateUSER", description='Generate a User File')
	parser.add_argument('-a',"--initialSystem",  type=str, default="9.10.cudf")
	parser.add_argument('-t',"--t0",  type=int, default="1256900400")
	parser.add_argument('-T',"--nDays",  type=int, default=365)
	parser.add_argument('-u',"--updatePROB",  type=float, default=0.0)
	parser.add_argument('-i',"--installPROB",  type=float , default=0.0)
	parser.add_argument('-w',"--weightedPackageFile",  type=str , default="weightedpackages")
	parser.add_argument('-U',"--updateMOF",  type=str , default="-removed,-new,-uptodatedistance")
	parser.add_argument('-I',"--installMOF",  type=str , default="-removed,-changed,-uptodatedistance")
	parser.add_argument('-r',"--repositoryFolder",  type=str , default="reps")
	parser.add_argument('-o',"--outFile",  type=str, required=True)
	
	args = parser.parse_args()

	#Validate arguments
	if not os.path.exists(args.initialSystem):
		print "initialSystem does not exist"
		exit()	
	
	if args.weightedPackageFile != None and not os.path.exists(args.weightedPackageFile):
		print "weightedPackageFile does not exist"
		exit()

	if args.weightedPackageFile != None and not os.path.exists(args.weightedPackageFile):
		print "weightedPackageFile does not exist"
		exit()
	
	#Assign args
	startdate = datetime.datetime.fromtimestamp(args.t0)
	days = args.nDays
	out = file(args.outFile,'w')
	
	#component weighted list
	clist =[]
	if args.weightedPackageFile != None:
		clines = open(args.weightedPackageFile).readlines()
		cweights = {}
		totalWeight = 0
		for line in clines:
			name,weight = line.split(",")
			assert weight != 0
			weight = float(weight)
			cweights[name] = weight	
			totalWeight += weight;

		clist = map(lambda x : (x,1.0 * cweights[x]/totalWeight),cweights.keys())

	out.write(args.initialSystem)
	out.write("\n")

	for d in range(0,days):
		ireq = ""
		ureq = ""
		#Install
		if random.random() < args.installPROB: 		
			comps, weights = zip(*clist)
			compindex = select_from_weights(weights)
			ireq = "install: " + clist[compindex][0]			
			clist.remove(clist[compindex])
			
		if random.random() < args.updatePROB:
			ureq = "upgrade: *"

		if ureq != "" or ireq != "":
			
				
			t = time.mktime((startdate + datetime.timedelta(days=d)).timetuple())
			repfile = getCudf(t,args.repositoryFolder)
			print repfile
			
			
			if ureq != "":
				out.write(str(t))
				out.write(";\t")
				out.write(ureq)
				out.write(";\t")
				out.write(repfile)
				out.write(";\t")
				out.write(args.updateMOF)
				out.write("\n")
			
			t += 60000
			if ireq != "":
				out.write(str(t))
				out.write(";\t")
				out.write(ireq)
				out.write(";\t")
				out.write(repfile)
				out.write(";\t")
				out.write(args.installMOF)
				out.write("\n")
			
	out.close()

	
def select_from_weights(weights):
	if len(weights) == 1: return 0;
	rnd = random.random() * sum(weights)
	for i, w in enumerate(weights):
		rnd -= w
		if rnd < 0:
			return i

def getCudf(t,reps):
	cs = sorted(os.listdir(reps))
	for c in cs:
		ct = int(c.split(".")[0])
		if ct >= t:
			return os.path.join(reps,c)
if __name__ == "__main__":
	main()
