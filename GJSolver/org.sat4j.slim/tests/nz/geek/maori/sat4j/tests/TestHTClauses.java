//package nz.geek.maori.sat4j.tests;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.List;
//
//import nz.geek.maori.sat4j.core.SolverFactory;
//import nz.geek.maori.sat4j.core.VecInt;
//import nz.geek.maori.sat4j.specs.IConstr;
//import nz.geek.maori.sat4j.specs.ISolver;
//import nz.geek.maori.sat4j.tests.parser.CNFParser;
//import nz.geek.maori.sat4j.tests.parser.Visitor;
//import nz.geek.maori.sat4j.tools.LiteralsUtils;
//
//import org.junit.Before;
//import org.junit.Test;
//
//public class TestHTClauses extends SolverTest {
//
//	@Before
//	public void before() {
//		this.sat4jtime = 0;
//		this.mytime = 0;
//
//	}
//
//	@Test
//	public void testHT() throws Exception {
//
//		IConstr u1 = this.dsf.createConstraint(new VecInt(new int[] { 7 }));
//		this.solver.addConstr(u1);
//
//		IConstr con1 = this.dsf.createConstraint(new VecInt(
//				new int[] { 3, 4, 6 }));
//		this.solver.addConstr(con1);
//
//		IConstr con2 = this.dsf.createConstraint(new VecInt(
//				new int[] { 5, 2, 6 }));
//		this.solver.addConstr(con2);
//
//		IConstr con3 = this.dsf.createConstraint(new VecInt(
//				new int[] { 7, 2, 4 }));
//		this.solver.addConstr(con3);
//
//		assertTrue(this.solver.isSatisfiable());
//
//		List model = getModel(this.solver);
//
//		assertTrue(model.contains(3));
//		assertTrue(model.contains(5));
//		assertTrue(model.contains(7));
//
//		IConstr con4 = this.dsf.createConstraint(new VecInt(
//				new int[] { 2, 4, 6 }));
//		this.solver.addConstr(con4);
//
//		assertTrue(this.solver.isSatisfiable());
//
//		model = getModel(this.solver);
//
//		assertTrue(model.contains(2));
//		assertTrue(model.contains(4));
//		assertTrue(model.contains(7));
//
//		IConstr con5 = this.dsf.createConstraint(new VecInt(
//				new int[] { 3, 5, 6 }));
//		this.solver.addConstr(con5);
//
//		assertFalse(this.solver.isSatisfiable());
//	}
//
//	@Test
//	public void testContradiction() throws Exception {
//
//		IConstr u1 = this.dsf.createConstraint(new VecInt(new int[] { 2 }));
//		this.solver.addConstr(u1);
//
//		IConstr u2 = this.dsf.createConstraint(new VecInt(new int[] { 3 }));
//		this.solver.addConstr(u2);
//
//		assertFalse(this.solver.isSatisfiable());
//
//	}
//
//	@Test
//	public void testAIM() throws Exception {
//
//		File f = new File("./problems/aim");
//		for (File nf : f.listFiles()) {
//			if (nf.getName().startsWith(".")) {
//				continue;
//			}
//			solverFile(!nf.getName().contains("-no-"), nf);
//			this.solver = SolverFactory.newDefault();
//		}
//
//		System.out.println("My time : " + this.mytime);
//		System.out.println("Sat4j time : " + this.sat4jtime);
//
//	}
//
//	@Test
//	public void testUUF() throws Exception {
//
//		File f = new File("./problems/uuf");
//		for (File nf : f.listFiles()) {
//			if (nf.getName().startsWith(".")) {
//				continue;
//			}
//			solverFile(!nf.getName().contains("-no-"), nf);
//			this.solver = SolverFactory.newDefault();
//		}
//
//		System.out.println("My time : " + this.mytime);
//		System.out.println("Sat4j time : " + this.sat4jtime);
//
//	}
//
//	long sat4jtime = 0;
//	long mytime = 0;
//
//	private void solverFile(boolean satisfiable, File f) throws Exception {
//		final ISolver sat4jSolver = org.sat4j.minisat.SolverFactory.instance()
//				.newBestHT();
//		Visitor sat4jVisitor = new Visitor() {
//
//			@Override
//			public void addClause(int[] clause) {
//				try {
//					sat4jSolver.addClause(new VecInt(clause));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
//
//		Visitor myVisitor = new Visitor() {
//
//			@Override
//			public void addClause(int[] clause) {
//				try {
//					VecInt constr = new VecInt();
//					for (int i : clause) {
//						constr.push(LiteralsUtils.toInternal(i));
//					}
//
//					// System.out.println(Arrays.toString(clause));
//					IConstr cons = TestHTClauses.this.dsf
//							.createConstraint(constr);
//					TestHTClauses.this.solver.addConstr(cons);
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
//
//		CNFParser.parse(new BufferedReader(new FileReader(f)), new Visitor[] {
//				myVisitor, sat4jVisitor });
//
//		long t2 = System.currentTimeMillis();
//		assertTrue(sat4jSolver.isSatisfiable() == satisfiable);
//		this.sat4jtime += System.currentTimeMillis() - t2;
//
//		long t1 = System.currentTimeMillis();
//		assertTrue(this.solver.isSatisfiable() == satisfiable);
//		this.mytime += System.currentTimeMillis() - t1;
//
//	}
//
//	@Test
//	public void testCardConstraints() throws Exception {
//
//		IConstr u1 = this.dsf.createConflictSet(new VecInt(
//				new int[] { 2, 4, 6 }));
//
//		this.solver.addConstr(this.dsf.createConstraint(new VecInt(new int[] {
//				2, 4, 6 })));
//
//		this.solver.addConstr(u1);
//		assertTrue(this.solver.isSatisfiable());
//
//		IConstr m1 = this.dsf
//				.createConstraint(new VecInt(new int[] { 3, 4, 6 }));
//		this.solver.addConstr(m1);
//		assertTrue(this.solver.isSatisfiable());
//
//		IConstr m2 = this.dsf
//				.createConstraint(new VecInt(new int[] { 2, 5, 6 }));
//		this.solver.addConstr(m2);
//		assertTrue(this.solver.isSatisfiable());
//
//		IConstr m3 = this.dsf
//				.createConstraint(new VecInt(new int[] { 2, 4, 7 }));
//		this.solver.addConstr(m3);
//		assertFalse(this.solver.isSatisfiable());
//
//	}
//}
