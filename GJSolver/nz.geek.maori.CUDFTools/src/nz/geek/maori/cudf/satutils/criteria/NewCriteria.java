package nz.geek.maori.cudf.satutils.criteria;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.satutils.PackageNameConstraints;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.ProfileChangeRequest;

public class NewCriteria extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> ones = new ArrayList<Long>();

	private HashSet<Literal> names = new HashSet<Literal>();

	private boolean minimise = true;

	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;

	public NewCriteria(boolean minimise) {
		this.minimise = minimise;
	}


	@Override
	public Collection<AbstractConstraint> init() {
		PackageNameConstraints instance = PackageNameConstraints.getInstance(cdh.getProfileChangeRequest());

		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();

		ProfileChangeRequest pcr = cdh.getProfileChangeRequest();
		for (String pn: pcr.getPackageNames(false)) {
			if(pcr.getInstalledPackageVersions(pn).size() == 0)
			{

				this.ones.add(1l);
				Object variable = instance.getNamedPackage(pn);
				this.names.add(new Literal(true,variable));
				prefs.put(variable,false);
				weights.put(variable, DEFAULTWEIGHT);
			}
		}

		return instance.generateNamedPackageConstraints();
	}

	@Override
	public Map<Object, Boolean> getPrefeeredLits(ProfileChangeRequest pcr) {

		return prefs;
	}

	@Override
	public Map<Object, Double> getWeightedLits() {
		return weights;
	}


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {

		int newc = countNew(model);

		AbstractConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(names), ones, false, newc);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		
		return cons;

	}


	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {

		Model model = vis.getModel();

		int newc = countNew(model);
		Logger.getLogger(getClass().getName()).log(Level.INFO,newc + " new packages");

		if(newc == 0) return false;


		AbstractConstraint lth = new AbstractPBConstraint(new ArrayList<Literal>(names), ones, false, newc - 1);
		List<AbstractConstraint> cons = new ArrayList<AbstractConstraint>();
		cons.add(lth);
		
		HashSet<Literal> n = new HashSet<Literal>();
		for (Literal lit : names) {
//			weights.put(lit.getVariable(), weights.get(lit.getVariable())*1000);
			if (model.getIns().contains(lit.getVariable())) {
				n.add(Literal.NEG(lit.getVariable()));
			}
		}
		
		cons.add(new AbstractSATConstraint(n));
		
		
		return vis.tryConstraints(cons);
	}


	private int countNew(Model model) {
		int newc = 0;
		//		prefs = new HashMap<Object, Boolean>();
		//		weights = new HashMap<Object, Double>();

		for (Literal lit : names) {
			// if it is not installed but was initially installed
			if (model.getIns().contains(lit.getVariable())) {
				newc++;
			}
		}
		return newc;
	}




}
