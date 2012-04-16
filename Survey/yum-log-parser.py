#!/usr/bin/python

import sys
import pickle
import os

logfile = sys.argv[1]
folder = "actionmaps"

from datetime import datetime

format = "%b %d %H:%M:%S"

rl = open(logfile,'r').readlines()
i = 0;
#actions = Map<DateTime,UserAction>
actions = {}

for line in rl :
	sp = line.split()
	if len(sp) < 4 :
		continue
	date = reduce(lambda x,y : x + " " + y,sp[:3])
	act = sp[3]
	d = datetime.strptime(date,format)

	#init d
	if not d in actions :
		actions[d] = {}
		actions[d]["install"] = 0;
		actions[d]["remove"] = 0;
		actions[d]["upgrade"] = 0;
		actions[d]["purge"] = 0;
		actions[d]["downgrade"] = 0;
		actions[d]["action"] = 0;

	if act.startswith("Installed:") :
		actions[d]["install"] = actions[d]["install"] + 1
	elif act.startswith("Erased:") :
		actions[d]["remove"] = actions[d]["remove"] + 1
	elif act.startswith("Updated:") :
		actions[d]["upgrade"] = actions[d]["upgrade"] + 1

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
