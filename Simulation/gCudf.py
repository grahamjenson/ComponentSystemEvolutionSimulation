#!/usr/bin/python
from cudfpy import cudfpkg
import os
import shutil
import sys
import argparse

__doc__ = """
g[enerate]CUDF is a script that takes a line from the user model, a list of installed cudf packages, and a cufd repository, to create a cudf problem
"""
	
def main():
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
	
	args.preamble = "preamble: \n" + args.preamble + "\n"
	args.userline = "\nrequest: \n" + args.userline
	
	
	#change to installed
	pcri = cudfpkg.createProfileChangeRequest(args.installFile)
	
	out = open(args.outFile,'w')
	out.write(args.preamble)
	
	
	f = open(args.repFile,"r")
	pv = [None,None]
	i = 0
	for line in f:
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
		
	#create request	
	out.write(args.userline)
	out.close()
	
if __name__ == "__main__":
	main()
