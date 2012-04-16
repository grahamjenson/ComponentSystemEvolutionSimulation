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

import nz.geek.maori.cudf.satutils.ChangedPackageConstraints;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;
public class ChangeCriteria extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> ones = new ArrayList<Long>();;
	private HashSet<Literal> names = new HashSet<Literal>();

	
	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;
	
	public ChangeCriteria(boolean minimise) {

	}

	// Map from name to a dimacs value that maps to constraint.

	@Override
	public Collection<AbstractConstraint> init() {
		ChangedPackageConstraints cpc = ChangedPackageConstraints.getInstance(this.cdh.getProfileChangeRequest());

		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();
		
		for (String name : this.cdh.getProfileChangeRequest().getPackageNames(false)) {
			this.ones.add(1l);
			Object variable = cpc.getChangedPackage(name);
			this.names.add(Literal.POS(variable));
			prefs.put(variable,true);
			weights.put(variable, DEFAULTWEIGHT);
		}

		return cpc.generateChangedPackageConstraints();
	}



	@Override
	public Map<Object, Double> getWeightedLits() {
		return weights;
	}
	
	@Override
	public Map<Object, Boolean> getPrefeeredLits(ProfileChangeRequest pcr) {

		return prefs;
	}


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {

		int changed = countChanged(model);
		int notchanged = names.size() - changed;

		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, true, notchanged);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		
		return cons;

	}

	
	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {
		Model model = vis.getModel();

		int changed = countChanged(model);
		int notchanged = names.size() - changed;
		Logger.getLogger(getClass().getName()).log(Level.INFO,changed + " packages  changed");

		if(changed == 0) return false;

		
		AbstractPBConstraint c1 = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, true, notchanged + 1);
		List<AbstractConstraint> c1s = Arrays.asList((AbstractConstraint)c1);
		boolean tryConstraints = vis.tryConstraints(c1s);
		return tryConstraints;
	}

	private int countChanged(Model model) {
		int changed = 0;
		for (Literal i : names) {
			// if it is not installed but was initially installed
			Object var = i.getVariable();
			if (model.getOuts().contains(var)) {
				changed++;
			}

		}
		return changed;
	}





}
