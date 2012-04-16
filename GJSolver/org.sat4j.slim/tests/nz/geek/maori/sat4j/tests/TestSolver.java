package nz.geek.maori.sat4j.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nz.geek.maori.sat4j.core.VecInt;
import nz.geek.maori.sat4j.specs.IConstr;

import org.junit.Test;

public class TestSolver extends SolverTest {

	@Test
	public void testReplay() throws Exception {
		IConstr a = this.dsf.createConstraint(new VecInt(new int[] { 2 }));
		IConstr nega = this.dsf.createConstraint(new VecInt(new int[] { 3 }));
		this.solver.addConstr(a);
		this.solver.addConstr(nega);

		assertFalse(this.solver.isSatisfiable());
		assertFalse(this.solver.isSatisfiable());

		this.solver.removeConstr(nega);
		assertTrue(this.solver.isSatisfiable());
	}
}
