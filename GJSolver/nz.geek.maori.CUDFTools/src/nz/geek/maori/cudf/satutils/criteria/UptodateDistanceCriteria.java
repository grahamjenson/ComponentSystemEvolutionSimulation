package nz.geek.maori.cudf.satutils.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.satutils.PackageNameConstraints;
import nz.geek.maori.cudf.satutils.UpdatedPackageConstraints;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;


import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Relation;


public class UptodateDistanceCriteria extends Criteria {


	private ArrayList<Long> ones = new ArrayList<Long>();

	private ArrayList<Literal> names = new ArrayList<Literal>();

	private boolean minimise = true;

	private Map<Object, Boolean> prefs;
	private Map<Object, Double> weights;

	public UptodateDistanceCriteria(boolean minimise) {
		this.minimise = minimise;
	}


	@Override
	public Collection<AbstractConstraint> init() {

		for(String pn : cdh.getProfileChangeRequest().getPackageNames(false))
		{
			ArrayList<Package> packs = new ArrayList<Package>(cdh.getProfileChangeRequest().getPackageVersions(pn));
			Collections.sort(packs, new Comparator<Package>() {

				@Override
				public int compare(Package o1, Package o2) {
					return o2.getVersion() - o1.getVersion();
				}
			});
			long i = 0;

			for(Package p : packs)
			{

				Literal pos = Literal.POS(p);
				names.add(pos);
				ones.add(i);
				i++;
			}

		}

		return new ArrayList<AbstractConstraint>();
	}

	@Override
	public Map<Object, Double> getWeightedLits() {
		return weights;
	}

	@Override
	public Map<Object,Boolean> getPrefeeredLits(ProfileChangeRequest pcr)
	{
		prefs = new HashMap<Object, Boolean>();
		weights = new HashMap<Object, Double>();

		for(String pn : cdh.getProfileChangeRequest().getPackageNames(false))
		{
			ArrayList<Package> packs = new ArrayList<Package>(cdh.getProfileChangeRequest().getPackageVersions(pn));
			Collections.sort(packs, new Comparator<Package>() {

				@Override
				public int compare(Package o1, Package o2) {
					return o1.getVersion() - o2.getVersion();
				}
			});

			int i = 0;
			Package high = null;
			for(Package p : packs)
			{


				Package ip = pcr.getPackage(p.getName(), p.getVersion());
				boolean installed = ip == null ? false : ip.isInstalled();
				if(installed)
				{
					high = p;
				}
				prefs.put(p, false);
				weights.put(p, DEFAULTWEIGHT);
				i++;
			}
			if(high != null)
			{

				for(int h = Math.min(packs.indexOf(high)+1,packs.size()-1); h < packs.size(); h++)
				{
					Package p1 = packs.get(h);
					prefs.put(p1,true);
					weights.put(p1, DEFAULTWEIGHT);				
				}

			}

		}

		return prefs;
	}


	@Override
	public Collection<AbstractConstraint> lockCurrentSolution(Model model) {

		int distance = countUpToDateDistance(model);

		AbstractPBConstraint eq = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false, distance);
		List<AbstractConstraint> cons = Arrays.asList((AbstractConstraint)eq);
		return cons;

	}

	@Override
	public boolean findBetterSolution(OptimiserVisitor vis) {

		Model model = vis.getModel();

		int distance = countUpToDateDistance(model);
		Logger.getLogger(getClass().getName()).log(Level.INFO,distance + " up to date distance");

		if(distance == 0) return false;



		AbstractPBConstraint lth = new AbstractPBConstraint(new ArrayList<Literal>(this.names), this.ones, false, distance - 1);
		List<AbstractConstraint> cons  = Arrays.asList((AbstractConstraint)lth);

		return vis.tryConstraints(cons);
	}

	private int countUpToDateDistance(Model model) {
		int distance = 0;

		for (int i = 0; i < names.size(); i++)
		{
			Literal lit = names.get(i);

			if (model.getIns().contains(lit.getVariable())) {
				//				Package p = (Package)lit.getVariable();

				distance += ones.get(i);
			}
		}
		return distance;
	}





}