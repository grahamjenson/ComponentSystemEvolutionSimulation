package nz.geek.maori.sat4j.core.config;

import java.io.PrintWriter;

import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IOrder;
import nz.geek.maori.sat4j.specs.IPhaseSelectionStrategy;

public abstract class AbstractOrder implements IOrder {

	protected ILits voc;

	@Override
	public void solutionFound() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLits(ILits lits) {
		this.voc = lits;
	}

	@Override
	public int select() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void undo(int x) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateVar(int p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void printStat(PrintWriter out, String prefix) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVarDecay(double d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void varDecayActivity() {
		// TODO Auto-generated method stub

	}

	@Override
	public double varActivity(int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void assignLiteral(int p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPhaseSelectionStrategy(IPhaseSelectionStrategy strategy) {
		// TODO Auto-generated method stub

	}

	@Override
	public IPhaseSelectionStrategy getPhaseSelectionStrategy() {
		// TODO Auto-generated method stub
		return null;
	}

}
