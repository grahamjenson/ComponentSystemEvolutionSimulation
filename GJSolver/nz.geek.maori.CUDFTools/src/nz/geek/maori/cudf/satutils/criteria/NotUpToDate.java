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
import nz.geek.maori.cudf.satutils.UpdatedPackageConstraints;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;

public class NotUpToDate extends Criteria {


	private ArrayList<Long> ones = new ArrayList<Long>();

	private HashSet<Literal> names = new HashSet<Literal>();

	private boolean minimise = true;

	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;
	
	public NotUpToDate(boolean minimise) {
		this.minimise = minimise;
	}


	@Override
	public Collection<AbstractConstraint> init() {
		UpdatedPackageConstraints instance = UpdatedPackageConstraints.getInstance(cdh.getProfileChangeRequest());
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();
		
		for(String pn : cdh.getProfileChangeRequest().getPackageNames(false))
		{
			Object variable = instance.getUpodatedPackageVariable(pn);
			Literal pos = Literal.POS(variable);
			names.add(pos);
			ones.add(1l);
			prefs.put(variable,false);
			weights.put(variable, DEFAULTWEIGHT);
		}

		return instance.getUpdatedPackageConstraints();
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

		int notuptodate = countNotUpToDate(model);

		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false, notuptodate);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		return cons;

	}

	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {

		Model model = vis.getModel();

		int notuptodate = countNotUpToDate(model);
		if(notuptodate == 0) return false;

		Logger.getLogger(getClass().getName()).log(Level.INFO,notuptodate + " packages not up to date");

		AbstractPBConstraint lth = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false, notuptodate - 1);
		List<AbstractConstraint> cons  = Arrays.asList((AbstractConstraint)lth);

		return vis.tryConstraints(cons);
	}

	private int countNotUpToDate(Model model) {
		int notuptodate = 0;

		for (Literal lit : names) {
			// if it is not installed but was initially installed

			if (model.getIns().contains(lit.getVariable())) {
				notuptodate++;
			}
		}
		return notuptodate;
	}





}

