package nz.geek.maori.sat4j.tests;

import java.util.ArrayList;
import java.util.List;

import nz.geek.maori.sat4j.constraints.factory.ConstraintFactory;
import nz.geek.maori.sat4j.core.Solver;
import nz.geek.maori.sat4j.core.SolverFactory;

import org.junit.Before;

public abstract class SolverTest {

	protected List getModel(Solver solver) {
		int[] m = solver.model();
		List model = new ArrayList();
		for (int element : m) {
			model.add(element);
		}
		return model;
	}

	Solver solver = null;
	ConstraintFactory dsf = ConstraintFactory.getInstance();

	@Before
	public void setUp() {
		this.solver = SolverFactory.newDefault();

	}
}
