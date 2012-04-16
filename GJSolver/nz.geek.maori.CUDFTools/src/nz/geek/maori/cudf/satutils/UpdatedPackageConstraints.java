package nz.geek.maori.cudf.satutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;

public class UpdatedPackageConstraints {

	private static UpdatedPackageConstraints INSTANCE; 
	private static ProfileChangeRequest PCR;
	
	public static UpdatedPackageConstraints getInstance(ProfileChangeRequest pcr)
	{
		if(INSTANCE == null || PCR != pcr)
		{
			PCR = pcr;
			INSTANCE = new UpdatedPackageConstraints(pcr);
		}
		return INSTANCE;

	}


	private HashMap<String, Object> updatedPackage = null;
	private ArrayList<Package> maxPacakges = new ArrayList<Package>();
	private ArrayList<AbstractConstraint> cons = null;
	private static final String PRE = "__UDATEDPACKAGE__";

	private UpdatedPackageConstraints(ProfileChangeRequest pcr)
	{
		cons = new ArrayList<AbstractConstraint>();
		PackageNameConstraints instance = PackageNameConstraints.getInstance(pcr);
		cons.addAll(instance.generateNamedPackageConstraints());

		updatedPackage = new HashMap<String, Object>();


		for (String pn : pcr.getPackageNames(false)) {
			// We need a variable that states the most updated version is included.
			// x is the varaible, p is the max version, and pn is the package constraint that is true if the package name is installed
			//x <=> pn and -p
			//x -> pn and -p == -x or (pn and -p) == (2)-x or pn AND (3)-x and -p
			//(pn and -p) -> x == -(pn and -p) or x == (1) -pn or p or x

			//get max version
			Package p = pcr.getGreatestVersion(pn).iterator().next();

			maxPacakges.add(p);

			String x = PRE + pn + " : " + p;	
			this.updatedPackage.put(pn, x);
			Object pnvar = instance.getNamedPackage(pn);

			AbstractSATConstraint one = new AbstractSATConstraint();
			one.getLits().add(Literal.POS(x));
			one.getLits().add(Literal.NEG(pnvar));
			one.getLits().add(Literal.POS(p));

			AbstractSATConstraint two = new AbstractSATConstraint();
			two.getLits().add(Literal.NEG(x));
			two.getLits().add(Literal.POS(pnvar));

			AbstractSATConstraint three = new AbstractSATConstraint();
			three.getLits().add(Literal.NEG(x));
			three.getLits().add(Literal.NEG(p));

			cons.add(one);
			cons.add(two);
			cons.add(three);

		}

		
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "created " + cons.size() + " updated packages constraints");
		
	}


	public List<Package> getMaxPacakges()
	{
		return maxPacakges;
	}

	public Object getUpodatedPackageVariable(String name) {
		return this.updatedPackage.get(name);
	}


	public Collection<AbstractConstraint> getUpdatedPackageConstraints() {
		return cons;
	}

}
