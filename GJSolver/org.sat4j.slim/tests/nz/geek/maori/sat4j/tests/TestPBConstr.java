package nz.geek.maori.sat4j.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nz.geek.maori.sat4j.core.VecInt;
import nz.geek.maori.sat4j.specs.IConstr;

import org.junit.Test;

public class TestPBConstr extends SolverTest {

	@Test
	public void testHT() throws Exception {

		IConstr u1 = this.dsf.createConstraint(new VecInt(new int[] { 7 }));
		this.solver.addConstr(u1);

		IConstr con1 = this.dsf.createConstraint(new VecInt(
				new int[] { 3, 4, 6 }));
		this.solver.addConstr(con1);

		IConstr con2 = this.dsf.createConstraint(new VecInt(
				new int[] { 5, 2, 6 }));
		this.solver.addConstr(con2);

		IConstr con3 = this.dsf.createConstraint(new VecInt(
				new int[] { 7, 2, 4 }));
		this.solver.addConstr(con3);

		assertTrue(this.solver.isSatisfiable());

		List model = getModel(this.solver);

		assertTrue(model.contains(3));
		assertTrue(model.contains(5));
		assertTrue(model.contains(7));

		IConstr con4 = this.dsf.createConstraint(new VecInt(
				new int[] { 2, 4, 6 }));
		this.solver.addConstr(con4);

		assertTrue(this.solver.isSatisfiable());

		model = getModel(this.solver);

		System.out.println(model);

		assertTrue(model.contains(2));
		assertTrue(model.contains(4));
		assertTrue(model.contains(7));

		IConstr con5 = this.dsf.createConstraint(new VecInt(
				new int[] { 3, 5, 6 }));
		this.solver.addConstr(con5);

		assertFalse(this.solver.isSatisfiable());
	}

	@Test
	public void testContradiction() throws Exception {

		IConstr u1 = this.dsf.createConstraint(new VecInt(new int[] { 2 }));
		this.solver.addConstr(u1);

		IConstr u2 = this.dsf.createConstraint(new VecInt(new int[] { 3 }));
		this.solver.addConstr(u2);

		assertFalse(this.solver.isSatisfiable());

	}

}
