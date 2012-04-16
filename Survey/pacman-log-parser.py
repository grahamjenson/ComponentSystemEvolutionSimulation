#!/usr/bin/python

import sys
import pickle
import os

logfile = sys.argv[1]
folder = "actionmaps"

from datetime import datetime

format = "%Y-%m-%d %H:%M"

rl = open(logfile,'r').readlines()
i = 0;
#actions = Map<DateTime,UserAction>
actions = {}

for line in rl :
	line = line[1:]
	sp = line.split("] ")
	if len(sp) != 2 :
		continue
	date = sp[0]
	act = sp[1]
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

	if act.startswith("installed") :
		actions[d]["install"] = actions[d]["install"] + 1
	elif act.startswith("removed") :
		actions[d]["remove"] = actions[d]["remove"] + 1
	elif act.startswith("upgraded") :
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
