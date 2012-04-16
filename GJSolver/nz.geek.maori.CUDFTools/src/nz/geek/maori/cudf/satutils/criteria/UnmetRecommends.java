package nz.geek.maori.cudf.satutils.criteria;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.CUDFDependencyHelper;
import nz.geek.maori.cudf.satutils.PackageNameConstraints;
import nz.geek.maori.cudf.satutils.ReccommendsConstraints;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;


public class UnmetRecommends extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> ones = new ArrayList<Long>();

	private HashSet<Literal> names = new HashSet<Literal>();

	private boolean minimise = true;

	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;
	
	public UnmetRecommends(boolean minimise) {
		this.minimise = minimise;
	}


	@Override
	public Collection<AbstractConstraint> init() {
		ReccommendsConstraints instance = ReccommendsConstraints.getInstance(cdh.getProfileChangeRequest());
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();
		
		for (Object name : instance.getUnsatReccommendsVariables()) {
			this.ones.add(1l);
			this.names.add(new Literal(true,name));
			prefs.put(name,false);
			weights.put(name, DEFAULTWEIGHT);
		}

		return instance.getUnSatReccommendsConstraints();
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

		int notrecommeds = countNotReccommends(model);


		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false, notrecommeds);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);

		return cons;
	}

	public boolean findBetterSolution(OptimiserVisitor vis) {

		Model model = vis.getModel();

		int notrecommeds = countNotReccommends(model);
		Logger.getLogger(getClass().getName()).log(Level.INFO,notrecommeds + " out of " + names.size() + " reccommendations not satisfied");
		if(notrecommeds == 0) return false;
		
		AbstractPBConstraint lth = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false, notrecommeds -  1);
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


	private int countNotReccommends(Model model) {
		int notrecommeds = 0;

		for (Literal lit : names) {
			if (model.getIns().contains(lit.getVariable())) {
				notrecommeds++;
			}
		}
		return notrecommeds;
	}






}
