#!/usr/bin/python

import shelve

summary = {}
for name,strat,c,crit in strats:
	summary[strat] = shelve.open("." + strat + ".shelve")


