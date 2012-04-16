package nz.geek.maori.cudf.simpleSolver;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.negLit;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.posLit;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.var;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.toDimacs;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.neg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.parser.Parser;
import nz.geek.maori.cudf.satutils.CUDFDependencyHelper;
import nz.geek.maori.cudf.satutils.constraints.AbstractConflictSet;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractPBConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.constraints.Model;
import nz.geek.maori.cudf.satutils.constraints.OptimiserVisitor;
import nz.geek.maori.cudf.satutils.criteria.AddedSumCriteria;
import nz.geek.maori.cudf.satutils.criteria.ChangeCriteria;
import nz.geek.maori.cudf.satutils.criteria.ChangedSumCriteria;
import nz.geek.maori.cudf.satutils.criteria.Criteria;
import nz.geek.maori.cudf.satutils.criteria.HammingCriteria;
import nz.geek.maori.cudf.satutils.criteria.InstallsPerPackageCriteria;
import nz.geek.maori.cudf.satutils.criteria.LexicographicCriteria;
import nz.geek.maori.cudf.satutils.criteria.NewComponentCriteria;
import nz.geek.maori.cudf.satutils.criteria.NewCriteria;
import nz.geek.maori.cudf.satutils.criteria.NotUpToDate;
import nz.geek.maori.cudf.satutils.criteria.OptimisationFunctions;
import nz.geek.maori.cudf.satutils.criteria.ProductCriteria;
import nz.geek.maori.cudf.satutils.criteria.RemoveCriteria;
import nz.geek.maori.cudf.satutils.criteria.RemovedSumCriteria;
import nz.geek.maori.cudf.satutils.criteria.StableCriteria;
import nz.geek.maori.cudf.satutils.criteria.SumCriteria;
import nz.geek.maori.cudf.satutils.criteria.SystemSizeCriteria;
import nz.geek.maori.cudf.satutils.criteria.UnmetRecommends;
import nz.geek.maori.cudf.satutils.criteria.UptodateDistanceCriteria;
import nz.geek.maori.sat4j.constraints.factory.ConstraintFactory;
import nz.geek.maori.sat4j.core.RestartStrategy;
import nz.geek.maori.sat4j.core.SearchParams;
import nz.geek.maori.sat4j.core.Solver;
import nz.geek.maori.sat4j.core.SolverFactory;
import nz.geek.maori.sat4j.core.Vec;
import nz.geek.maori.sat4j.core.VecInt;
import nz.geek.maori.sat4j.core.config.PreferencePhaseSelector;
import nz.geek.maori.sat4j.core.config.VarOrderHeap;
import nz.geek.maori.sat4j.core.config.WeightedVarOrderHeap;
import nz.geek.maori.sat4j.specs.ContradictionException;
import nz.geek.maori.sat4j.specs.IConstr;
import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IPhaseSelectionStrategy;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.IVecInt;

public class SimpleSolver {

	static ConstraintFactory dsf = ConstraintFactory.getInstance();
	static boolean returnPreviousSystemOnFail = true;
	
	public static void printHelp() {
		System.out.println("Grahams Solver");
		System.out
		.println("Usage: GrahamsSolver inputfile outputfile critieralist timeout");
		System.out.println("criteria :: (+|-)criteron [(,|.) (+|-) criterion]*\n" +
				"criteron :: named | sum(property) | added(property) | removed(property)\n" +
				"named :: removed | new |  changed | notuptodate | " +
				"unsat_recommends | hamming | uptodatedistance | removed_components | uptodate-metacomp\n" +
		"property :: pagerank | hubs | authority | instability | predictiveuse\n");
		System.exit(0);
	}

	public static Criteria parseCriterion(String s)
	{
		if(!s.startsWith("-") && !s.startsWith("+"))
		{
			throw new UnsupportedOperationException("Criteria prefix incorrect : " +s);
		}

		//is named or property bsaed, if named will not contain (
		boolean named = !s.contains("(");
		if(named)
		{
			if (s.substring(1).equals("removed"))
			{
				RemoveCriteria rc = new RemoveCriteria(s.startsWith("-"));
				return rc;
			} else if (s.substring(1).equals("changed")) {
				ChangeCriteria cc = new ChangeCriteria(s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("notuptodate")) {
				NotUpToDate cc = new NotUpToDate(s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("unsat_recommends")) {
				UnmetRecommends cc = new UnmetRecommends(s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("new")) {
				NewCriteria cc = new NewCriteria( s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("ss")) {
				SystemSizeCriteria cc = new SystemSizeCriteria( s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("ipp")) {
				InstallsPerPackageCriteria cc = new InstallsPerPackageCriteria( s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("hamming")) {
				HammingCriteria cc = new HammingCriteria( s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("newc")) {
				NewComponentCriteria cc = new NewComponentCriteria( s.startsWith("-"));
				return cc;
			} else if (s.substring(1).equals("uptodatedistance")) {
				UptodateDistanceCriteria cc = new UptodateDistanceCriteria( s.startsWith("-"));
				return cc;
			}
		}
		else
		{
			if (s.substring(1).startsWith("sum"))
			{
				String property = s.substring(5,s.length()-1);
				SumCriteria sc = new SumCriteria(s.startsWith("-"), property);
				return sc;

			}
			else if (s.substring(1).startsWith("removed"))
			{
				String property = s.substring(9,s.length()-1);
				RemovedSumCriteria sc = new RemovedSumCriteria(s.startsWith("-"), property);
				return sc;

			}
			else if (s.substring(1).startsWith("changed"))
			{
				String property = s.substring(9,s.length()-1);
				ChangedSumCriteria sc = new ChangedSumCriteria(s.startsWith("-"), property);
				return sc;

			}
			else if (s.substring(1).startsWith("added"))
			{
				String property = s.substring(7,s.length()-1);
				AddedSumCriteria sc = new AddedSumCriteria(s.startsWith("-"), property);
				return sc;

			} else if (s.substring(1).startsWith("stableversion")) {
				int  age = Integer.parseInt(s.substring(15,s.length()-1));
				StableCriteria cc = new StableCriteria(age);
				return cc;
			}
		}

		throw new UnsupportedOperationException("Unsupported criteria: " +s);


	}


	/**
	 * Args[0] is the input file args[1] is the output file TODO args[2]
	 * optimisation criteria
	 * 
	 * Different Lexicographic constraints, Lexicographically ordered Literals,
	 * Pseudo boolean optimisation
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 4 && args.length != 3) {
			printHelp();
			return;
		}



		OutputStreamWriter outputWriter;
		final CUDFDependencyHelper cdh;
		//Parse Arguments output :: CUDF, OutputWriter, ArrayList<Criteria> 

		File inputFile;
		LexicographicCriteria lex;
		// File Checks
		{ 

			inputFile = new File(args[0]);
			if (!inputFile.exists()) {
				throw new FileNotFoundException("Input File does not Exist");
			}

			File outputFile = new File(args[1]);
			if (outputFile.exists()) {
				outputFile.delete();
			}
			outputFile.createNewFile();
			outputWriter = new OutputStreamWriter(new FileOutputStream(outputFile),
					Charset.forName("US-ASCII"));
		}
		//Parse Criteria
		{
			lex = new LexicographicCriteria();
			String[] lexlist = args[2].split(",");

			for (String l : lexlist) {
				//Check Prefix
				String[] prodlist = l.split("\\.");
				if(prodlist.length > 1)
				{
					ProductCriteria pc = new ProductCriteria();
					for (String p : prodlist)
					{
						pc.addCriteria(parseCriterion(p));
					}
					lex.addCriteria(pc);
				}
				else
				{
					lex.addCriteria(parseCriterion(l));
				}

			}
		}
		//Parse CUDF create CDH



		SimpleSolver ss = new SimpleSolver();
		long timeout = 120000;
		if(args.length == 4)
		{
			timeout = Long.parseLong(args[3]) - 5000;
		}
		System.out.println("Timeout " + timeout);
		ProfileChangeRequest finalPCR = ss.solve(inputFile,lex,timeout);

		//Write output
		{
			if (finalPCR == null || finalPCR.getUniverse().size() == 0) {
				// Have failed
				outputWriter.write("FAIL");
				outputWriter.close();
				System.out.println("FAIL");
				return;
			}

			// System.out.println("Initial Size of Solution was " +
			// pcr.getInstalledPackages().size());
			// System.out.println("Size of Solution is " +
			// finalPCR.getInstalledPackages().size());

			System.out.println("System Size: " + finalPCR.getInstalledPackages().size());
			outputWriter.write(finalPCR.toCUDF());
			outputWriter.close();
		}
	}

	public ProfileChangeRequest solve(File inputFile, Criteria criteria, final long timeout)
	{
		final long starttime = System.currentTimeMillis();

		ProfileChangeRequest pcr = Parser.parse(inputFile);

		final CUDFDependencyHelper cdh = new CUDFDependencyHelper(pcr,criteria);
		//Solve output :: finalPCR

		final Solver s = SolverFactory.newDefault();

		//Set up Solver
		{
			ArrayList<AbstractConstraint> initialConstraints = new ArrayList<AbstractConstraint>();
			initialConstraints.addAll(cdh.generateKeepConstraints());
			initialConstraints.addAll(cdh.generteComponentConstraints());
			initialConstraints.addAll(cdh.generteRequestConstraints());
			initialConstraints.addAll(cdh.generateCriteriaConstraints());

			for(AbstractConstraint ac : initialConstraints)
			{
				try {
					s.addConstr(abstractConstrToRealConstr(ac,s.getVocabulary()));
				} catch (ContradictionException e) {
					e.printStackTrace();
					return null;
				}
			}

		}

		//setup timeout
		Thread timer;
		//final long timeout = 285000;
		{

			Runnable timerRunner = new Runnable() {

				@Override
				public void run() {
					try {
						Thread.currentThread();
						long t1 = System.currentTimeMillis();
						long millis = timeout - (t1-starttime);
						Thread.sleep(millis);
						System.out.println("TimeOut Reached");
						s.expireTimeout();
					} catch (InterruptedException e) {
					}

				}
			};

			timer = new Thread(timerRunner);
			timer.start();
		}

		//Loop


		Logger.getAnonymousLogger().log(Level.INFO, "Solving");




		final OptimiserVisitor vis = new OptimiserVisitor() {

			Model m = null;
			Collection<IConstr> allcons = new HashSet<IConstr>();

			@Override
			public Model getModel() {
				if(m == null)
				{
					if(!s.isSatisfiable())
					{
						Logger.getAnonymousLogger().log(Level.INFO,"Bounded in "+ (System.currentTimeMillis() - starttime) + "ms");

						m = null;
						return null;
					}

					Logger.getAnonymousLogger().log(Level.INFO,"Solution Found in "+ (System.currentTimeMillis() - starttime) + "ms");

					int[] model = s.model();
					HashSet<Object> ins = new HashSet<Object>();
					HashSet<Object> outs = new HashSet<Object>();
					for (int i : model) {
						int d = toDimacs(i);
						if (d > 0) 
						{

							Object obj = getObject(d);
							ins.add(obj);
						}
						else
						{
							Object obj = getObject(Math.abs(d));
							outs.add(obj);
						}
					}
					m = new Model(ins,outs);
				}
				return m;
			}

			@Override
			public boolean tryConstraints(Collection<AbstractConstraint> acons) {
				updateOrder();

				Collection<IConstr> cons = new ArrayList<IConstr>();


				for(AbstractConstraint as : acons)
				{
					try {
						IConstr acon = abstractConstrToRealConstr(as,s.getVocabulary());
						cons.add(acon);
						allcons.add(acon);
						s.addConstr(acon);
					}
					catch(Exception e)
					{
						for(IConstr con : cons)
						{
							allcons.remove(con);
							s.removeConstr(con);
						}
						return false;	
					}
				}
				Model oldModel = m;
				m = null;
				m = this.getModel();
				if(m == null)
				{
					m = oldModel;
					for(IConstr con : cons)
					{
						allcons.remove(con);
						s.removeConstr(con);
					}
					return false;
				}

				//Maintain what we have learnt
				//				IVecInt learnedLiterals = s.getLearnedLiterals();
				//				for(int i = 0; i < learnedLiterals.size(); i++ )
				//				{
				//					IConstr con = ConstraintFactory.getInstance().createConstraint(new VecInt(new int[]{learnedLiterals.get(i)}));
				//					s.addConstr(con);
				//				}
				//				learnedLiterals.clear();

				return true;
			}




			@Override
			public boolean addNonConflistingConstraints(Collection<AbstractConstraint> acons) {
				Collection<IConstr> cons = new ArrayList<IConstr>();
				for(AbstractConstraint as : acons)
				{
					try {
						IConstr acon = abstractConstrToRealConstr(as,s.getVocabulary());
						cons.add(acon);
						allcons.add(acon);
						s.addConstr(acon);
					}
					catch(Exception e)
					{
						for(IConstr con : cons)
						{
							allcons.remove(con);
							s.removeConstr(con);

						}
						return false;	
					}
				}

				//assert s.isSatisfiable();

				return true;
			}

			@Override
			public boolean isTimeout() {
				return System.currentTimeMillis() - starttime > timeout;
			}

			@Override
			public void updateOrder() {
				ProfileChangeRequest pcr = cdh.getProfileChangeRequest();
				if(m != null)
				{
					pcr = m.toPCR();
				}

				Map<Object, Boolean> critprefs = cdh.getCriteria().getPrefeeredLits(pcr);
				Map<Object, Double> critweights = cdh.getCriteria().getWeightedLits();

				Map<Integer,Boolean> prefs = new HashMap<Integer, Boolean>();

				for(Object o : critprefs.keySet())
				{
					int var = getVariable(o, s.getVocabulary());
					prefs.put(var, critprefs.get(o));

				}
				Map<Integer,Double> weights = new HashMap<Integer, Double>();

				for(Object o : critweights.keySet())
				{
					int var = getVariable(o, s.getVocabulary());
					weights.put(var, critweights.get(o));

				}

				WeightedVarOrderHeap order = new WeightedVarOrderHeap(new PreferencePhaseSelector(prefs),weights);

				s.setOrder(order);

			}


			public void clear()
			{
				for(IConstr c : allcons)
				{
					s.removeConstr(c);
				}
				allcons = new HashSet<IConstr>();
			}
		};



		Criteria crit = cdh.getCriteria();
		vis.updateOrder();
		Model m = vis.getModel(); 
		if(m != null)
		{
			vis.updateOrder();
			do
			{	
				m = vis.getModel();
				System.out.println(m.toPCR().getInstalledPackages().size());
				//s.simplifyDB();
				//vis.clear();
				vis.addNonConflistingConstraints(crit.lockCurrentSolution(m));

			}while(System.currentTimeMillis() - starttime < timeout && crit.findBetterSolution(vis));
		}


		timer.interrupt();
		System.out.println("time it took + "
				+ (System.currentTimeMillis() - starttime));

		if(m != null)
		{
			return m.toPCR();
		}
		System.out.println("FAIL");
		if(returnPreviousSystemOnFail)
		{
			return getMinimalProfileChangeRequest(pcr);
		}
		return null;
	}


	//	public static IConstr negateToMinimalSolution(int[] model) {
	//		VecInt notSameSolution = new VecInt();
	//		for (int lit : model) {
	//			int dim = toDimacs(lit);
	//			Object pack = getObject(Math.abs(dim));
	//			if ((pack instanceof Package) && (dim > 0)) {
	//				notSameSolution.push(neg(lit));
	//			}
	//
	//		}
	//		return dsf.createConstraint(notSameSolution);
	//	}

	// Only negates the components, meaning all extra lits cant be different
	public IConstr negateSolution(int[] model) {
		VecInt notSameSolution = new VecInt();
		for (int lit : model) {
			int dim = toDimacs(lit);
			Object pack = getObject(Math.abs(dim));
			if ((pack instanceof Package) && (dim > 0)) {
				notSameSolution.push(neg(lit));
			}

		}
		return dsf.createConstraint(notSameSolution);
	}

	HashMap<Object, Integer> map = new HashMap<Object, Integer>();
	HashMap<Integer, Object> revmap = new HashMap<Integer, Object>();

	public int getVariable(Object obj, ILits lits) {
		if (obj == null) {
			throw new UnsupportedOperationException();
		}
		Integer i = map.get(obj);
		if (i == null) {
			i =  lits.getFreeVariable();
			map.put(obj, i);
			revmap.put(i, obj);
		}
		return i;
	}

	public  Object getObject(int var)
	{
		return revmap.get(var);
	}

	public IConstr abstractConstrToRealConstr(AbstractConstraint as, ILits lits) throws ContradictionException
	{
		if(as instanceof AbstractPBConstraint)
		{
			AbstractPBConstraint apb = (AbstractPBConstraint)as;
			VecInt literals = new VecInt();
			for (Literal i : apb.getLits()) {
				int var = getVariable(i.getVariable(), lits);
				int lit = i.isPhase() ? posLit(var) : negLit(var);
				literals.push(lit);
			}

			Vec<BigInteger> coefs = new Vec<BigInteger>();
			for (Long i : apb.getCoefs()) {
				coefs.push(BigInteger.valueOf(i));
			}


			return dsf.createPseudoBooleanConstraint(literals, coefs,
					apb.isMoreThan(), BigInteger.valueOf(apb.getDegree()));

		}
		else if(as instanceof AbstractSATConstraint)
		{
			AbstractSATConstraint asc = (AbstractSATConstraint)as;
			VecInt literals = new VecInt();
			for (Literal i : asc.getLits()) {
				int var = getVariable(i.getVariable(), lits);
				int lit = i.isPhase() ? posLit(var) : negLit(var);
				literals.push(lit);
			}
			return dsf.createConstraint(literals);
		}
		else if(as instanceof AbstractConflictSet)
		{
			AbstractConflictSet asc = (AbstractConflictSet)as;
			VecInt literals = new VecInt();
			for (Literal i : asc.getLits()) {
				int var = getVariable(i.getVariable(), lits);
				int lit = i.isPhase() ? posLit(var) : negLit(var);
				literals.push(lit);
			}
			return dsf.createConflictSet(literals);
		}
		throw new UnsupportedOperationException();
	}


	public ProfileChangeRequest getMinimalProfileChangeRequest(ProfileChangeRequest pcr) {
		ProfileChangeRequest newPCR = CUDFFactory.eINSTANCE.createProfileChangeRequest();
		// newPCR.setPreamble(pcr.getPreamble());
		for (Package pack : pcr.getInstalledPackages())
		{
					pack = pack.clone();
					pack.setInstalled(true);
					newPCR.addPackage(pack);
		}
		return newPCR;
	}

}