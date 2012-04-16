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
import nz.geek.maori.cudf.satutils.ChangedPackageConstraints;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

public class InstallsPerPackageCriteria extends Criteria {

	// Internal list of named constraints
	
	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;
	
	public InstallsPerPackageCriteria(boolean minimise) {

	}

	// Map from name to a dimacs value that maps to constraint.

	@Override
	public Collection<AbstractConstraint> init() {
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();
		
		for (Package p : this.cdh.getProfileChangeRequest().getUniverse()) {
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


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {
		
		HashMap<String,Integer> installed = countInstalled(model);
		ArrayList<AbstractConstraint> constraints = new ArrayList<AbstractConstraint>();
		
		for(String name : this.cdh.getProfileChangeRequest().getPackageNames(false))
		{
			int total = 1;
			if(installed.containsKey(name))
			{
				total = installed.get(name);
			}
			
			ArrayList<Literal> lits = new ArrayList<Literal>();
			ArrayList<Long> ones = new ArrayList<Long>();
			for(Package p : this.cdh.getProfileChangeRequest().getPackageVersions(name))
			{
				ones.add(1l);
				lits.add(Literal.POS(p));
			}
			AbstractPBConstraint pb = new AbstractPBConstraint(lits, ones, false, total);
			constraints.add(pb);
		}
		
		
		
		return constraints;
		
	}

	private HashMap<String,Integer> countInstalled(Model model)
	{
		HashMap<String,Integer> installed = new HashMap<String, Integer>();
		for(Object o : model.getIns())
		{
			if(o instanceof Package)
			{
				Package p = (Package) o;
				if(!installed.containsKey(p.getName()))
				{
					installed.put(p.getName(), 0);
				}
				installed.put(p.getName(), installed.get(p.getName())+1);
			}
		}
		return installed;
	}
	
	private HashSet<String> badnames = new HashSet<String>();
	
	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {
		Model model = vis.getModel();
		HashMap<String,Integer> installed = countInstalled(model);
		for(String name : installed.keySet())
		{
			Integer total = installed.get(name);
			if(total > 1 && !badnames.contains(name))
			{
				ArrayList<AbstractConstraint> constraints = new ArrayList<AbstractConstraint>();
				ArrayList<Literal> lits = new ArrayList<Literal>();
				ArrayList<Long> ones = new ArrayList<Long>();
				for(Package p : this.cdh.getProfileChangeRequest().getPackageVersions(name))
				{
					ones.add(1l);
					lits.add(Literal.POS(p));
				}
				AbstractPBConstraint pb = new AbstractPBConstraint(lits, ones, false, total-1);
				constraints.add(pb);
				Logger.getLogger(getClass().getName()).log(Level.INFO,name + " lowering to " + (total-1));
				if(vis.tryConstraints(constraints))
				{
					return true;
				}
				else
				{
					badnames.add(name);
				}
			}
		}
		
		return false;
	}

}
