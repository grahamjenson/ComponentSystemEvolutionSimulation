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
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

public class SystemSizeCriteria extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> ones = new ArrayList<Long>();

	private HashSet<Literal> names = new HashSet<Literal>();


	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;
	
	
	public SystemSizeCriteria(boolean minimise) {
	}



	@Override
	public Collection<AbstractConstraint> init() {
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();
		
		for (Package p : this.cdh.getProfileChangeRequest().getUniverse()) {
			this.ones.add(1l);
			this.names.add(new Literal(true,p));
			prefs.put(p,false);
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
		int ss = countsystemSize(model);
		
		Logger.getLogger(getClass().getName()).log(Level.INFO,ss + " system size");
		
		if(ss == 0) return false;
		
		AbstractPBConstraint lth = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false,ss - 1);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)lth);
		
		return vis.tryConstraints(cons);
			
	}



	private int countsystemSize(Model model) {
		int c = 0;
		for(Object o : model.getIns())
		{
			if(o instanceof Package)
			{
				c++;
			}
		}
		return c;
	}


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {
		int ss = countsystemSize(model);


		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false,ss);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		
		return cons;
	}
}
