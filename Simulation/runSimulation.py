#!/usr/bin/python
from cudfpy import cudfpkg
import os
import shutil
import sys
import getopt
import subprocess

__doc__ = """
Usage: runsimulation 
 -i initial installation
 -c installation criteria
 -u update criteria
 -n name of criteria
 -t timeout
"""
	
def main():
	try:
		opts, args = getopt.getopt(sys.argv[1:], "hi:c:u:n:s:t:", [])
	except getopt.error, msg:
		print msg
		print "for help use -h"
		sys.exit(2)

	initialCUDF = None
	criteria = None
	criterianame = None
	timeout = 150000
	updatecriteria = None
	
	# process options
	for o, a in opts:
		if o == ("-h"):
			print __doc__
			sys.exit(0)
		elif o == "-i":
			initialCUDF = a
		elif o == "-c":
			criteria = a
		elif o == "-n":
			criterianame = a
		elif o == "-u":
			updatecriteria = a
		elif o == "-t":
			timeout = int(a)

	#validate input
	error = False;
	if initialCUDF == None:
		print "no init cudf file"
		error = True
	elif not os.path.exists(initialCUDF):
		print "init cudf file doesnt exist"
		error = True

		
	if criteria == None:
		print "no crit"
		error = True
	
	if updatecriteria == None:
		print "no update crit"
		error = True
		
	if criterianame == None:
		print "no crit name"
		error = True
		
	if error : 
		sys.exit(1)
	
	preamble = "property: recommends: vpkgformula, age: int, hubs: int, auth: int, ca: int, ce: int, instability: int, metains: int, metaouts: int, metaversion: int, pagerank: int"
	
	reps = os.listdir("reps")
	reps.sort()
	reps.reverse()
	userFiles = os.listdir("users")
	userFiles.sort()
	userIndex = 0
	for user in userFiles:
		userIndex +=1
		print "Examining user: ", user, userIndex,"/",len(userFiles)
		installed = initialCUDF
		outputfolder = "solutions/" + criterianame + "/" + user+"/"
		if os.path.exists(outputfolder):
			print "Partially done: ",user
		else:				
			os.makedirs(outputfolder)
		
		dayIndex = 0
		userLines = open("users/" +user).readlines()
		for line in userLines:
			dayIndex += 1
			time,install,keep,update = line.split(";")
			update = update.strip()		
			#must install or update something for the cudf to be run	
			if (install == '' and update == '') : continue
			
			repository = None;
			for x in reps:
				if float(x) <= float(time):
					repository = x;
					break;
			
			if update != '':
				outputcudf = outputfolder+time+"-update.cudf"
				if os.path.exists(outputcudf):
					print "skip update already done"
				else:
					print "building update cudf", dayIndex, "/", len(userLines)
					cudffile = "." + criterianame+ "-tmp-update.cudf"
					process = subprocess.Popen(["./gCudf.py", "-p",preamble, "-u",time+";;"+keep+";update","-i", installed, "-r","reps/"+repository,"-o",cudffile])
					process.wait()
			
					print "executing update for ",criterianame
				
					process = subprocess.Popen(["./execute_solver","gjsolver",cudffile,outputcudf,updatecriteria,str(timeout)])
					process.wait()
					
				#If it fails we just ignore the attempt and move on
				line = open(outputcudf).readline().strip()
				if line.startswith("# List of Installed Packages") :
					installed = outputcudf
				else:
					print "failed update"
			
			if install != '':
				outputcudf = outputfolder+time+".cudf"
				if os.path.exists(outputcudf):
					print "skip install already done"
					
				else:
					installNumber = 0;	
					print "Building cudf", dayIndex, "/", len(userLines)
					cudffile = "." + criterianame+ "-tmp.cudf"
					process = subprocess.Popen(["./gCudf.py", "-p",preamble, "-u",time+";" + install + ";" + keep+";","-i", installed, "-r","reps/"+repository,"-o",cudffile])
					process.wait()
			
					print "executing solver for ", criterianame
			
				
					process = subprocess.Popen(["./execute_solver","gjsolver",cudffile,outputcudf,criteria,str(timeout)])
					process.wait()
				
				#If it fails we just ignore the attempt and move on
				line = open(outputcudf).readline().strip()
				if line.startswith("# List of Installed Packages") :
					installed = outputcudf
				else:
					print "failed install"
				
	
if __name__ == "__main__":
	main()
