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
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;

public class HammingCriteria extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> ones = new ArrayList<Long>();

	private HashSet<Literal> names = new HashSet<Literal>();


	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;
	
	
	public HammingCriteria(boolean minimise) {
	}



	@Override
	public Collection<AbstractConstraint> init() {
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();
		
		for (Package p : this.cdh.getProfileChangeRequest().getUniverse()) {
			this.ones.add(1l);
			this.names.add(new Literal(!p.isInstalled(),p));
			prefs.put(p,p.isInstalled());
			weights.put(p, DEFAULTWEIGHT);
		}

		return new ArrayList<AbstractConstraint>();
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
		int ham = countHamming(model);
		
		Logger.getLogger(getClass().getName()).log(Level.INFO,ham + " hamming change");
		
		if(ham == 0) return false;
		
		AbstractPBConstraint lth = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false,ham - 1);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)lth);
		
		return vis.tryConstraints(cons);
			
	}



	private int countHamming(Model model) {
		int hamming = 0;

		for (Literal name : names) {
			// if it is not installed but was initially installed
			Package p  = (Package)name.getVariable();
			
			if(model.getIns().contains(p))
			{
				if(!p.isInstalled())
				{
					hamming++;
					weights.put(p, DEFAULTWEIGHT*10000);
				}
			}
			
			if(model.getOuts().contains(p))
			{
				if(p.isInstalled())
				{
					hamming++;
					weights.put(p, DEFAULTWEIGHT*10000);
				}
			}
		}
		return hamming;
	}



	


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {
		int ham = countHamming(model);


		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false,ham);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		
		return cons;
	}
}
