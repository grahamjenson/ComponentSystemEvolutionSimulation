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

public class ChangedPackageConstraints {


	private static ChangedPackageConstraints INSTANCE; 
	private static ProfileChangeRequest PCR;
	
	public static ChangedPackageConstraints getInstance(ProfileChangeRequest pcr)
	{
		if(INSTANCE == null || PCR != pcr)
		{
			PCR = pcr;
			INSTANCE = new ChangedPackageConstraints(pcr);
			
		}
		return INSTANCE;
		
	}
	
	private HashMap<String, Object> changedPackage = null;
	private HashMap<Object, String> packageNames = null;
	private ArrayList<AbstractConstraint> cons = null;
	private static final String PRE = "__CHANGEDNAME__";
	
	private ChangedPackageConstraints(ProfileChangeRequest pcr)
	{
		cons = new ArrayList<AbstractConstraint>();
		changedPackage = new HashMap<String, Object>();
		packageNames = new HashMap<Object, String>();
		for (String pn : pcr.getPackageNames(false)) {
			Collection<Package> packageVersions = pcr.getPackageVersions(pn);
			if (packageVersions.size() == 1) {
				// If there is only one version then no need for the complexity
				Package p = packageVersions.iterator().next();
				if(p.isInstalled())
				{
					this.changedPackage.put(pn, p);
					this.packageNames.put(p, pn);
				}
				else
				{
					//Because if there is only one package installed and these variables represent wheteher they have not changed, 
					//not being installed means that it has not changed, so not(x) <=> p
					//Here the package d <=> (-p)
					//so d -> -p equals -d or -p
					// and -p -> d equals p or d 
					String d = PRE + pn;
					this.changedPackage.put(pn, d);
					this.packageNames.put(d, pn);
				
					AbstractSATConstraint pos = new AbstractSATConstraint();
					pos.getLits().add(new Literal(false, d));
					pos.getLits().add(new Literal(false,p));
					cons.add(pos);

					AbstractSATConstraint not = new AbstractSATConstraint();
					not.getLits().add(new Literal(true,d));
					not.getLits().add(new Literal(true,p));
					cons.add(not);
				}

			} else {

				String d = PRE + pn;
				this.changedPackage.put(pn, d);
				this.packageNames.put(d, pn);
				// d is the Var, and internalD = +d

				// given a1 and a2 are initial component literals with the same name
				// we need x <=> a1 AND a2
				// which is  x -> (a1 AND a2) and (a1 AND a2) -> x
				// -x OR (a1 and a2) equals -x OR a1 AND -x OR a2
				// -(a1 AND a2) OR x equals x OR -a1 OR -a2 

				// So require, x or a1 a2
				// and d or -a1 AND d or -a2
				AbstractSATConstraint all = new AbstractSATConstraint();
				all.getLits().add(Literal.POS(d));

				for (Package na : packageVersions) {
					
					boolean inst = na.isInstalled();
					all.getLits().add(new Literal(!inst,na));

					// -x or a
					AbstractSATConstraint loc = new AbstractSATConstraint();
					loc.getLits().add(Literal.NEG(d));
					loc.getLits().add(new Literal(inst,na));
					cons.add(loc);

				}
				cons.add(all);
			}
		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "created " + cons.size() + " change package constraints");
	}
	

	
	public Collection<AbstractConstraint> generateChangedPackageConstraints() {
		return cons;
	}

	public Object getChangedPackage(String name) {
		return this.changedPackage.get(name);
	}
	
	public String getPacakgeName(Object variable) {
		return this.packageNames.get(variable);
	}
	
	public Collection<AbstractConstraint> generateNamedPackageConstraints() {
		return cons;
	}
	
}
