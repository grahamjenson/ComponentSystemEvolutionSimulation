all: 0 1 2 3

0:
	mkdir q5a/alwaysupdate.1209600.user.sols;./gjsolver q5a/alwaysupdate.1209600.user q5a/alwaysupdate.1209600.user.sols 1> q5a/alwaysupdate.1209600.user.out 2> q5a/alwaysupdate.1209600.user.err 

1:
	mkdir q5a/alwaysupdate.1814400.user.sols;./gjsolver q5a/alwaysupdate.1814400.user q5a/alwaysupdate.1814400.user.sols 1> q5a/alwaysupdate.1814400.user.out 2> q5a/alwaysupdate.1814400.user.err 

2:
	mkdir q5a/alwaysupdate.2419200.user.sols;./gjsolver q5a/alwaysupdate.2419200.user q5a/alwaysupdate.2419200.user.sols 1> q5a/alwaysupdate.2419200.user.out 2> q5a/alwaysupdate.2419200.user.err 

3:
	mkdir q5a/alwaysupdate.604800.user.sols;./gjsolver q5a/alwaysupdate.604800.user q5a/alwaysupdate.604800.user.sols 1> q5a/alwaysupdate.604800.user.out 2> q5a/alwaysupdate.604800.user.err 

