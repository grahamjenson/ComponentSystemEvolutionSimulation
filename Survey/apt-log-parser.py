#!/usr/bin/python

import sys
import pickle
import os

logfile = sys.argv[1]
folder = "actionmaps"

from datetime import datetime

format = "%Y-%m-%d %H:%M:%S"

rl = open(logfile,'r').readlines()
i = 0;
#actions = Map<DateTime,<UserAction,Size>>
actions = {}

while i < len(rl) :
	line = rl[i]
	i = i+1
	if line.startswith("Start-Date:") :
		d = datetime.strptime(line.split(":",1)[1].strip(),format)
		# init date key		
		if not d in actions :
			actions[d] = {}
			actions[d]["install"] = 0;
			actions[d]["remove"] = 0;
			actions[d]["upgrade"] = 0;
			actions[d]["purge"] = 0;
			actions[d]["downgrade"] = 0;
			actions[d]["action"] = 0;
		while not(line.startswith("End-Date:")) :
			line = rl[i]
			i = i +1
			if line.startswith("Install:") :
				actions[d]["install"] = actions[d]["install"] + len(line.split(","))
			elif line.startswith("Remove:") :
				actions[d]["remove"] = actions[d]["remove"] + len(line.split(","))
			elif line.startswith("Upgrade:") :
				actions[d]["upgrade"] = actions[d]["upgrade"] + len(line.split(","))
			elif line.startswith("Purge:") :
				actions[d]["purge"] = actions[d]["purge"] + len(line.split(","))
			elif line.startswith("Downgrade:") :
				actions[d]["downgrade"] = actions[d]["downgrade"] + len(line.split(","))
			elif line.startswith("Commandline:") :
				actions[d]["action"] = line.split(":",1)[1].strip()
			elif line.startswith("Start-Date:") or line.strip() == "":
				#Shouldnt happen but it does
				i = i -1
				break;
			elif not(line.startswith("End-Date:") or line.startswith("Error:")) :
				print "ERROR Unkown line: " + str(d) + " ; " + line.split(":",1)[0]

actionmapfile = os.path.join(folder,os.path.basename(logfile)+".actionmap")
actionlistfile = os.path.join(folder,os.path.basename(logfile)+".actionlist")

f = open(actionmapfile , 'w')
pickle.dump(actions,f)
f.close()

f = open(actionlistfile , 'w')
keys = actions.keys()
keys.sort()
for k in keys:
	f.write(str(k) + " : install " + str(actions[k]["install"]) + ", remove " + str(actions[k]["remove"]) + ", upgrade " + str(actions[k]["upgrade"]))
	f.write("\r\n")
f.close()
