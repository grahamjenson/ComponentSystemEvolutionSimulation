package nz.geek.maori.sat4j.core;

import nz.geek.maori.sat4j.core.config.ArminRestarts;
import nz.geek.maori.sat4j.core.config.MiniSATLearning;
import nz.geek.maori.sat4j.core.config.RSATPhaseSelectionStrategy;
import nz.geek.maori.sat4j.core.config.VarOrderHeap;

public class SolverFactory {

	public static Solver newDefault() {
		MiniSATLearning minisatlearning = new MiniSATLearning();
		Solver pbsolverresolution = new Solver(minisatlearning,
				new VarOrderHeap(new RSATPhaseSelectionStrategy()),
				new ArminRestarts());
		minisatlearning.setSolver(pbsolverresolution);

		return pbsolverresolution;
	}
}
