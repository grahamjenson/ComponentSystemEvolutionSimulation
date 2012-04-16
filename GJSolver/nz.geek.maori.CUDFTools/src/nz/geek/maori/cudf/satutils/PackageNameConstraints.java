package nz.geek.maori.cudf.satutils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;

public class PackageNameConstraints {
	
	
	
	
	private static PackageNameConstraints INSTANCE; 
	private static ProfileChangeRequest PCR;
	
	public static PackageNameConstraints getInstance(ProfileChangeRequest pcr)
	{
		if(INSTANCE == null || PCR != pcr)
		{
			PCR = pcr;
			INSTANCE = new PackageNameConstraints(pcr);
			
		}
		return INSTANCE;
		
	}
	
	
	private HashMap<String, Object> namedPackage = null;
	private HashMap<Object, String> packageNames = null;
	private ArrayList<AbstractConstraint> cons = null;
	private static final String PRE = "__PACKAGENAME__";
	
	private PackageNameConstraints(ProfileChangeRequest pcr)
	{
		cons = new ArrayList<AbstractConstraint>();
		namedPackage = new HashMap<String, Object>();
		packageNames = new HashMap<Object, String>();
		
		for (String pn : pcr.getPackageNames(false)) {
			Collection<Package> packageVersions = pcr.getPackageVersions(pn);
			if (packageVersions.size() == 1) {
				// If there is only one version then no need for the complexity
				Package p = packageVersions.iterator().next();
				this.namedPackage.put(p.getName(), p);
				packageNames.put(p, p.getName());
			} else {
				// We need a variable that is true when a name is in the
				// solution and false when it has been removed

				
				
				// d is the Var, and internalD = +d


				// given a1 and a2 are components with the same name
				// we need d <=> a1 or a2 == d -> a1 or a2 AND a1 or a2 -> d
				// d->a1 or a2 = -d or +a1 or +a2
				// a1 or a2 -> d = -(a1 or a2) or d :: De morgans
				// (-a1 AND -a2) or d = -a1 or d AND -a2 or d Distribitivity

				// So require, -d or +a1 +a2
				// and d or -a1 d or -a2

				// 2a -> 1b + 1c >= 1
				String d = PRE + pn;
				this.namedPackage.put(pn, d);
				packageNames.put(d,pn);
				AbstractSATConstraint all = new AbstractSATConstraint();
				
				all.getLits().add(new Literal(false,d));

				for (Package na : packageVersions) {
					all.getLits().add(new Literal(true,na));

					// d or -a
					AbstractSATConstraint loc = new AbstractSATConstraint();
					loc.getLits().add(new Literal(true,d));
					loc.getLits().add(new Literal(false,na));
					
					cons.add(loc);

				}
				cons.add(all);
				
				
			}
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "created " + cons.size() + " package name constraints");
	}

	
	public Object getNamedPackage(String name) {
		return this.namedPackage.get(name);
	}
	
	public String getPackageName(Object variable)
	{
		return packageNames.get(variable);
	}
	
	public Collection<AbstractConstraint> generateNamedPackageConstraints() {
		return cons;
	}
}
