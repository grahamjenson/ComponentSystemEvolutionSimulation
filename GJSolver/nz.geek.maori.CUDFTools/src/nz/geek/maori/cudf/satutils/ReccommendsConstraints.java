package nz.geek.maori.cudf.satutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageFormula;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;

public class ReccommendsConstraints {



	private static ReccommendsConstraints INSTANCE;

	private static ProfileChangeRequest PCR;

	public static ReccommendsConstraints getInstance(ProfileChangeRequest pcr)
	{
		if(INSTANCE == null || PCR != pcr)
		{
			PCR = pcr;
			INSTANCE = new ReccommendsConstraints(pcr);
		}
		return INSTANCE;

	}

	private ArrayList<AbstractConstraint> cons = null;
	private static final String PREX = "__PACKAGELISTNAME__";
	private static final String PREY = "__RECNAME__";
	
	private ArrayList<String> refs = new ArrayList<String>();
	
	private HashMap<HashSet<Package>,String> pfs = new HashMap<HashSet<Package>,String>();
	
	private ReccommendsConstraints(ProfileChangeRequest pcr)
	{


		cons = new ArrayList<AbstractConstraint>();


		for (Package p : pcr.getUniverse()) {
			
			//To limit the amount of constraints created we are cacheing packagelists, and assigning them literals
			//so if p recommends a1 or a2
			//so x <=> a1 or a2
			//and y <=> p and -x
			
			//therefore
			//x -> a1 or a2 :: (1) -x or a1 or a2
			//(a1 or a2) -> x :: -(a1 or a2) or x :: (-a1 AND -a2) or x :: (2) x or -a1 AND x or -a2
			
			//y -> p and ix :: -y or (p and -x) :: (3) -y or p AND (4) -y or -x 
			//(p and -x) -> y :: -(p and -x) or y :: (5) -p or x or y
			
			Object recobject = p.getProperties().get("recommends");
			if(recobject == null) continue;

			PackageFormula pf = (PackageFormula)recobject;
			if(pf.getAnd().size() == 0) continue;


			for(PackageList pl : pf.getAnd())
			{
				String x = null;
				Collection<Package> packagesThatSatisfy = pcr.getPackagesThatSatisfy(pl);
				HashSet<Package> hs = new HashSet<Package>(packagesThatSatisfy);
				//Get / Create x
				if(pfs.containsKey(hs))
				{
					x = pfs.get(hs);
	
				}
				else
				{
					
					x = PREX + pl.toString();

					this.pfs.put(hs,x);
					
					AbstractSATConstraint one = new AbstractSATConstraint();
					one.getLits().add(Literal.NEG(x));
					
					for(Package a : packagesThatSatisfy)
					{
						one.getLits().add(Literal.POS(a));
						
						AbstractSATConstraint two = new AbstractSATConstraint();
						two.getLits().add(Literal.POS(x));
						two.getLits().add(Literal.NEG(a));
						
						cons.add(two);
						
					}
					cons.add(one);	
				}
				

				//create Y variable
				String y = PREY + p + x;
				AbstractSATConstraint three = new AbstractSATConstraint();
				three.getLits().add(Literal.NEG(y));
				three.getLits().add(Literal.POS(p));
				cons.add(three);
				
				AbstractSATConstraint four = new AbstractSATConstraint();
				four.getLits().add(Literal.NEG(y));
				four.getLits().add(Literal.NEG(x));
				cons.add(four);
				
				AbstractSATConstraint five = new AbstractSATConstraint();
				five.getLits().add(Literal.NEG(p));
				five.getLits().add(Literal.POS(x));
				five.getLits().add(Literal.POS(y));
				cons.add(five);
				
				refs.add(y);
			}

		}
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, "created " + cons.size() + " reccommends constraints");
	}

	public Collection<AbstractConstraint> getUnSatReccommendsConstraints() {
		return cons;
	}


	public List<String> getUnsatReccommendsVariables() {

		return refs;
	}

}
