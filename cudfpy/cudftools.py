#!/usr/bin/python
import os
import pickle
import sys
import getopt
from cudfpkg import CUDFPackage
from cudfpkg import ProfileChangeRequest
import cudfpkg

__doc__ = """
CUDFTools is a package in which cudf can operate on CUDF Files
--Add installed packages
"""
	
def main():
	try:
		opts, args = getopt.getopt(sys.argv[1:], "hai:o:r:", [])
	except getopt.error, msg:
		print msg
		print "for help use -h"
		sys.exit(2)

	outputFile = None
	inputFile = None
	installs = None
	# process options
	for o, a in opts:
		if o == ("-h"):
			print __doc__
			sys.exit(0)
		elif o == "-i" :
			inputFile = a
		elif o == "-o":
			outputFile = a
		elif o == "-r":
			installs = a 
	#validate input


	pcr = cudfpkg.createProfileChangeRequest(inputFile)
	pcri = cudfpkg.createProfileChangeRequest(installs)
	npcr = ProfileChangeRequest()
	for p in pcri.getUniverse():
		np = pcr.getPackagesThatSatisfy((p.name,p.version,"="))[0]
		np.installed = True
		npcr.add(np)
		
	print npcr

if __name__ == "__main__":
	main()
