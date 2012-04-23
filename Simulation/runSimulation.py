#!/usr/bin/python
from cudfpy import cudfpkg
import os
import shutil
import sys
import getopt
import subprocess
import argparse
import gzip

def main():
	parser = argparse.ArgumentParser(prog="generateCUDF", description='Generate a CUDF File')
	parser.add_argument('-u',"--userFile",  type=str, required=True)
	parser.add_argument('-t',"--timeOut",  type=int, default=150000)
	parser.add_argument('-o',"--outputFolder", default=None)
	args = parser.parse_args()

	#Validate arguments
	if not os.path.exists(args.userFile):
		print "userFile does not exist"
		exit()	

	if args.outputFolder == None:
		args.outputFolder = os.path.dirname(args.userFile)

	#now do stuff
	
	userLines = open(args.userFile).readlines()
	previoussystem = userLines[0].strip()
	
	for line in userLines[1:]:
		
		time,request,rep,crit = line.split(";")
		time = time.strip()
		request = request.strip()
		rep = rep.strip()
		crit = crit.strip()
		
		

		outputcudf = os.path.join(args.outputFolder,time+"." + os.path.basename(args.userFile) + ".cudfsystem")
		print "making CUDF file"
		cudffile = generateCUDF(args.userFile,previoussystem,rep,request)

		print "executing",time,request,crit
		process = subprocess.Popen(["./execute_solver","gjsolver",cudffile,outputcudf,crit,str(args.timeOut)])
		process.wait()
			
		#If it fails we just ignore the attempt and move on
		line = open(outputcudf).readline().strip()
		previoussystem = outputcudf

def generateCUDF(usefilename, previousSystem,repFile,request):
	preamble = "preamble: \n"
	preamble += "property: recommends: vpkgformula, age: int, hubs: int, auth: int, ca: int, ce: int, instability: int, metains: int, metaouts: int, metaversion: int, pagerank: int\n\n"
	
	request = "\nrequest: \n" + request
	
	
	#change to installed
	pcri = cudfpkg.createProfileChangeRequest(previousSystem)
	tmpcudf = os.path.join(os.path.dirname(usefilename),"."+os.path.basename(usefilename)+ ".tmpcudf")
	out = open(tmpcudf ,'w')

	out.write(preamble)
	f = None
	if repFile.endswith(".gz"):
		f = gzip.open(repFile,'r')
	else:
		f = open(repFile,"r")
	
	pv = [None,None]
	i = 0
		
	line = f.readline()
	while line:
		out.write(line)
		if line.startswith("package:"):
			pv[0] = line[len("package: "):].strip()
		elif line.startswith("version:"):
			pv[1] = line[len("version: "):].strip()
		
		if pv[0] != None and pv[1] != None:
			if len(pcri.getPackagesThatSatisfy((pv[0],pv[1],"="))) > 0:
				i += 1
				out.write("installed: true\n")
				
			pv = [None,None]
		line = f.readline()
		
	#create request	
	out.write(request)
	out.close()
	return tmpcudf

if __name__ == "__main__":
	main()
