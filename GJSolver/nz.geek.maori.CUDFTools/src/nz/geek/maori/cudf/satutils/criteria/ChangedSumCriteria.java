package nz.geek.maori.cudf.satutils.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

public class ChangedSumCriteria extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> vals = new ArrayList<Long>();

	private ArrayList<Literal> names = new ArrayList<Literal>();

	private String property;
	private double mean;
	private double std;

	private Map<Object, Boolean> prefs;
	private Map<Object,Double> weights;

	public ChangedSumCriteria(boolean minimise,String property) {
		this.property = property;
	}


	@Override
	public Collection<AbstractConstraint> init() {
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();

		for(String name : this.cdh.getProfileChangeRequest().getPackageNames(false))
		{

			for(Package p : this.cdh.getProfileChangeRequest().getPackageVersions(name))
			{
				long min = Math.round(getPropertyValue(p)); // if there are no installed versions then it is maximum
				if(!p.isInstalled())
				{
					for(Package pi : this.cdh.getProfileChangeRequest().getPackageVersions(name, true))
					{
						long val = Math.round(Math.abs(getPropertyValue(p) - getPropertyValue(pi)));
						if(val < min)
						{
							min = val;
						}
					}
					names.add(Literal.POS(p));
					vals.add(min);
					weights.put(p, min*DEFAULTWEIGHT);
					prefs.put(p,false);
				}
				else
				{
					weights.put(p, DEFAULTWEIGHT*100);
					prefs.put(p,true);
				}



			}
		}
		return new ArrayList<AbstractConstraint>();
	}

	@Override
	public Map<Object, Boolean> getPrefeeredLits(ProfileChangeRequest pcr) {
		return prefs;
	}

	@Override
	public Map<Object, Double> getWeightedLits() {
		return weights;
	}

	private double getPropertyValue(Package p) {
		Map<String, Object> properties = p.getProperties();
		Object object = properties.get(property);
		if(object == null) return 0;
		Double dval = (Double)object;
		return dval;
	}


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {


		long value = sumProperty(model);


		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.vals, false, value);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);

		return cons;
	}

	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {

		Model model = vis.getModel();

		long value = sumProperty(model);

		Logger.getLogger(getClass().getName()).log(Level.INFO,value + " sum of change to " + property);

		if( value == 0)
		{
			return false;
		}

		AbstractPBConstraint c1 = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.vals, false,  value - 1);
		List<AbstractConstraint> c1s = Arrays.asList((AbstractConstraint)c1);

		return vis.tryConstraints(c1s);
	}



	private long sumProperty(Model model) {

		long sum = 0;

		for (int i = 0 ; i < names.size(); i++) {
			Literal lit = names.get(i);

			if(model.getIns().contains(lit.getVariable()))
			{
				long value = vals.get(i);
				sum += value;
			}

		}

		return sum;
	}


}
