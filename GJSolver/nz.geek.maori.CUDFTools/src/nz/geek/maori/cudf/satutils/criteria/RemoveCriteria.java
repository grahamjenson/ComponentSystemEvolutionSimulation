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

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.CUDFDependencyHelper;
import nz.geek.maori.cudf.satutils.ChangedPackageConstraints;
import nz.geek.maori.cudf.satutils.PackageNameConstraints;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

public class RemoveCriteria extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> ones = new ArrayList<Long>();

	private HashSet<Literal> names = new HashSet<Literal>();

	private boolean minimise = true;

	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;
	
	
	public RemoveCriteria(boolean minimise) {
		this.minimise = minimise;
	}



	@Override
	public Collection<AbstractConstraint> init() {
		PackageNameConstraints instance = PackageNameConstraints.getInstance(cdh.getProfileChangeRequest());
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();
		
		for (String name : this.cdh.getProfileChangeRequest().getPackageNames(true)) {
			this.ones.add(1l);
			Object variable = instance.getNamedPackage(name);
			this.names.add(new Literal(true,variable));
			prefs.put(variable,true);
			weights.put(variable, DEFAULTWEIGHT);
		}
		return instance.generateNamedPackageConstraints();
	}

	@Override
	public Map<Object, Double> getWeightedLits() {
		return weights;
	}

	
	@Override
	public Map<Object, Boolean> getPrefeeredLits(ProfileChangeRequest pcr) {
		return prefs;
	}

	// Map from name to a dimacs value that maps to constraint.

	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {


		Model model = vis.getModel();
		int removed = countRemoved(model);
		
		Logger.getLogger(getClass().getName()).log(Level.INFO,removed + " packages removed");
		
		if(removed == 0) return false;
		
		AbstractPBConstraint lth = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, true,(this.names.size() - removed) + 1);
		List<AbstractConstraint> c1s = Arrays.asList((AbstractConstraint)lth);
		
		boolean tryConstraints = vis.tryConstraints(c1s);
		
		return tryConstraints;
			
	}



	private int countRemoved(Model model) {
		int removed = 0;

		for (Literal name : names) {
			// if it is not installed but was initially installed
			if(model.getOuts().contains(name.getVariable()))
			{
				removed++;
			}
		}
		return removed;
	}



	


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {
		int removed = countRemoved(model);


		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, true,(this.names.size() - removed));
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		
		return cons;
	}

}
