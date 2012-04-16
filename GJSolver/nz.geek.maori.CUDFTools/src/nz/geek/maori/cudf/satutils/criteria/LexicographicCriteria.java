package nz.geek.maori.cudf.satutils.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

public class LexicographicCriteria extends Criteria{


	private List<Criteria> criteria = new ArrayList<Criteria>();
	private int currentCriteria = 0;

	public LexicographicCriteria() {
		super();
	}

	public void addCriteria(Criteria c)
	{
		this.criteria.add(c);
	}
	
	public boolean findBetterSolution(OptimiserVisitor vis) {

		if(vis.isTimeout()) return false;



		Criteria crit = criteria.get(currentCriteria);
		if(!crit.findBetterSolution(vis))
		{
			currentCriteria++;
			
			
			if(currentCriteria >= criteria.size())
			{
				return false;
			}
			vis.addNonConflistingConstraints(this.lockCurrentSolution(vis.getModel()));
			vis.updateOrder();
			return this.findBetterSolution(vis);
		}

		return true;
	}

	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model m) {
		HashSet<AbstractConstraint> hs = new HashSet<AbstractConstraint>();
		for(int i = currentCriteria; i >=0 ; i--)
		{
			hs.addAll(criteria.get(i).lockCurrentSolution(m));
		}
		return hs;
	}


	@Override
	public Map<Object, Boolean> getPrefeeredLits(ProfileChangeRequest pcr) {
		HashMap<Object,Boolean> prefs = new HashMap<Object, Boolean>();
		//Count backwards so the most important variables overright the prefeered phase of anything lower
		for(int i = currentCriteria; i >=0 ; i--)
		{
			Map<Object, Boolean> prefeeredLits = criteria.get(i).getPrefeeredLits(pcr);
			for(Object key : prefeeredLits.keySet())
			{
				prefs.put(key, prefeeredLits.get(key));
			}
		}
		return prefs;
		//		return criteria.get(currentCriteria).getPrefeeredLits();
	}


	@Override
	public Map<Object, Double> getWeightedLits() {
		HashMap<Object,Double> weights = new HashMap<Object, Double>();
		//Count backwards so the most important variables overright the prefeered phase of anything lower
		for(int i = currentCriteria; i >=0 ; i--)
		{
			Map<Object, Double> prefeeredLits = criteria.get(i).getWeightedLits();
			for(Object key : prefeeredLits.keySet())
			{
				//				double value = prefeeredLits.get(key)*1/(Math.pow(10,i));
				double value = prefeeredLits.get(key) * (criteria.size()-i)*10000;
				weights.put(key, value);
//				if(criteria.get(i).getClass().getName().equals("nz.geek.maori.cudf.satutils.criteria.NewCriteria"))
//					System.out.println(criteria.get(i).getClass().getName() + " : " + value);
			}
		}
		return weights;
		//return criteria.get(currentCriteria).getWeightedLits();
	}


	@Override
	public Collection<AbstractConstraint> init() {
		HashSet<AbstractConstraint> criteriaConstraints = new HashSet<AbstractConstraint>();
		for(Criteria c: criteria)
		{
			c.setDependencyHelper(this.cdh);
			//Some of these constraints may be duplicates
			criteriaConstraints.addAll(c.init());
		}
		return criteriaConstraints;
	}





}
