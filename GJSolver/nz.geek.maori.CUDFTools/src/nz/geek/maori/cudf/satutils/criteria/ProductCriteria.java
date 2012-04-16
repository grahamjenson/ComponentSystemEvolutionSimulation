package nz.geek.maori.cudf.satutils.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

public class ProductCriteria extends Criteria{


	private List<Criteria> criteria = new ArrayList<Criteria>();
	private Set<Criteria> maxed = new HashSet<Criteria>();
	
	private int prevCriteria = -1;
	
	
	public void addCriteria(Criteria c)
	{
		this.criteria.add(c);
	}
	
	public boolean findBetterSolution(OptimiserVisitor vis) {
		
		if(vis.isTimeout()) return false;
		if(criteria.size() == 0) return false;
		if(maxed.size() == criteria.size()) return false;
		
		//add one
		prevCriteria = (prevCriteria +1)%criteria.size();
		
		while(maxed.contains(criteria.get(prevCriteria)))
		{
			prevCriteria = (prevCriteria +1)%criteria.size();
		}
		
		Criteria crit = criteria.get(prevCriteria);
		
		
		
		if(!crit.findBetterSolution(vis))
		{
			maxed.add(crit);
			return this.findBetterSolution(vis);
		}
		
		return true;
	}

	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {
		HashSet<AbstractConstraint> hs = new HashSet<AbstractConstraint>();
		for(Criteria c : criteria)
		{
			hs.addAll(c.lockCurrentSolution(model));
		}
		return hs;
	}
	

	@Override
	public Map<Object, Boolean> getPrefeeredLits(ProfileChangeRequest pcr) {
		HashMap<Object,Boolean> prefs = new HashMap<Object, Boolean>();
		for(Criteria c : criteria)
		{
			Map<Object, Boolean> prefeeredLits = c.getPrefeeredLits(pcr);
			for(Object key : prefeeredLits.keySet())
			{
				prefs.put(key, prefeeredLits.get(key));
			}
		}
		return prefs;
	}

	@Override
	public Map<Object, Double> getWeightedLits() {
		HashMap<Object,Double> weights = new HashMap<Object, Double>();
		for(Criteria c : criteria)
		{
			Map<Object, Double> prefeeredLits = c.getWeightedLits();
			for(Object key : prefeeredLits.keySet())
			{
				weights.put(key, prefeeredLits.get(key));
			}
		}
		return weights;
	}

	
	@Override
	public Collection<AbstractConstraint> init() {
		HashSet<AbstractConstraint> criteriaConstraints = new HashSet<AbstractConstraint>();
		for(Criteria c: criteria)
		{
			c.setDependencyHelper(this.cdh);
			criteriaConstraints.addAll(c.init());
		}
		return criteriaConstraints;
	}




}
