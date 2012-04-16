package nz.geek.maori.cudf.satutils.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;

public class StableCriteria extends Criteria {

	private int age = 28;

	public StableCriteria(int age)
	{
		this.age = age;
	}

	@Override
	public Collection<AbstractConstraint> init() {
		ArrayList<AbstractConstraint> cons = new ArrayList<AbstractConstraint>();
		HashSet<String> names = new HashSet<String>(); 
		for(Package p : cdh.getProfileChangeRequest().getNotInstalledPackages())
		{
			if(Double.valueOf(p.getProperties().get("age").toString()) < this.age)
			{
				names.add(p.getName());
			}
		}
		//Freeze this package from being updated till it is at least 'age' old
		for(String name : names)
		{
			for(Package p : cdh.getProfileChangeRequest().getPackageVersions(name))
			{
				if(!p.isInstalled())
				{
					ArrayList<Literal> lits = new ArrayList<Literal>();
					lits.add(Literal.NEG(p));
					cons.add(new AbstractSATConstraint(lits));
				}
			}


		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "created " + cons.size() + " age based constraints for age " + this.age);
		return cons;
	}

	@Override
	public Map<Object, Boolean> getPrefeeredLits(ProfileChangeRequest pcr) {
		return new HashMap<Object, Boolean>();
	}

	@Override
	public Map<Object, Double> getWeightedLits() {
		return new HashMap<Object, Double>();
	}

	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {
		return false;
	}

	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model m) {
		return new ArrayList<AbstractConstraint>();
	}

}
