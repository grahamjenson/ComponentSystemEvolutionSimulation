package nz.geek.maori.cudf.satutils.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.CUDFDependencyHelper;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

public abstract class Criteria {


	protected CUDFDependencyHelper cdh = null;

	
	protected static final double DEFAULTWEIGHT = Double.MAX_VALUE/100000;
	
	public Criteria() {
		super();
	}

	public abstract Collection<AbstractConstraint> init();
	
	public abstract Map<Object,Boolean> getPrefeeredLits(ProfileChangeRequest pcr);
	
	
	public abstract Map<Object,Double> getWeightedLits();
	
	/**
	 * Method used to generate a constraint given a solution The first argument
	 * in the constraint is model <= possibleModel The second argument in the
	 * constraint is model < possibleModel
	 * 
	 * @param model
	 * @return if null then there is no better constraint
	 */
	public abstract boolean findBetterSolution(OptimiserVisitor vis);

	public abstract Collection<AbstractConstraint> lockCurrentSolution(Model m);
	
	public void setDependencyHelper(CUDFDependencyHelper dependencyHelper) {
		this.cdh = dependencyHelper;
	}

}
