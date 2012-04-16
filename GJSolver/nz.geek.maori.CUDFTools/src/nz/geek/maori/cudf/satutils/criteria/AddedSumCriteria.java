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

public class AddedSumCriteria extends Criteria {

	// Internal list of named constraints
	private ArrayList<Long> vals = new ArrayList<Long>();

	private ArrayList<Literal> names = new ArrayList<Literal>();

	private boolean minimise = true;
	private String property;
	private double mean;
	private double std;
	
	private Map<Object, Boolean> prefs;
	private Map<Object,Double> weights;
	
	public AddedSumCriteria(boolean minimise,String property) {
		this.minimise = minimise;
		this.property = property;
	}


	@Override
	public Collection<AbstractConstraint> init() {
		int n = 0;
		double mean = 0;
		double s = 0;
		
		for(Package p : this.cdh.getProfileChangeRequest().getNotInstalledPackages())
		{
			n++;
			names.add(Literal.POS(p));
			double x = getPropertyValue(p);
			vals.add(Math.round(x));

			double delta = x - mean;
			mean += delta /n;
			s += delta * (x - mean);
		}
		double varience = s/n;
		this.mean = mean;
		this.std = Math.sqrt(varience);
		
		
		 prefs = new HashMap<Object, Boolean>();
		 weights = new HashMap<Object, Double>();
		//preffer to keep installed packages
			
		for(Package p : this.cdh.getProfileChangeRequest().getNotInstalledPackages())
		{
			double x = getPropertyValue(p);
			weights.put(p, x*1.0);
			prefs.put(p,!minimise);
			if(x < mean + (2* std) && x > mean - (2*std))
			{
				continue;
			}
			//If the value is less than the mean we want it in else we dont
			prefs.put(p,minimise ? x < mean : x > mean);
			weights.put(p, x*1000.0);
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


		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.vals, !minimise, value);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		
		return cons;
	}
	
	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {

		Model model = vis.getModel();

		long value = sumProperty(model);
			
		Logger.getLogger(getClass().getName()).log(Level.INFO,value + " added sum of package " + property);
		
		
		if(minimise && value == 0)
		{
			return false;
		}
		
		AbstractPBConstraint c1 = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.vals, !minimise, minimise ? value - 1 : value + 1);
		List<AbstractConstraint> c1s = Arrays.asList((AbstractConstraint)c1);
			
		return vis.tryConstraints(c1s);
	}



	private long sumProperty(Model model) {
		
		long sum = 0;

		for (Literal l : names) {
			if(!model.getIns().contains(l.getVariable())) continue;
			long value = Math.round(getPropertyValue((Package)l.getVariable()));
			sum += value;
		}
		
		return sum;
	}








}
