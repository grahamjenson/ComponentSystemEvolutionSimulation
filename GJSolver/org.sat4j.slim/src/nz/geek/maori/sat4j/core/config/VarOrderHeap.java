/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 * 
 * Based on the original MiniSat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 * 
 *******************************************************************************/
package nz.geek.maori.sat4j.core.config;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.toInternal;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.var;

import java.io.PrintWriter;

import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IOrder;
import nz.geek.maori.sat4j.specs.IPhaseSelectionStrategy;

/*
 * Created on 16 oct. 2003
 */

/**
 * @author leberre Heuristique du prouveur. Changement par rapport au MiniSAT
 *         original : la gestion activity est faite ici et non plus dans Solver.
 */
public class VarOrderHeap implements IOrder {

	private static final double VAR_RESCALE_FACTOR = 1e-100;

	private static final double VAR_RESCALE_BOUND = 1 / VAR_RESCALE_FACTOR;

	/**
	 * mesure heuristique de l'activite d'une variable.
	 */
	protected double[] activity = new double[1];

	private double varDecay = 1.0;

	/**
	 * increment pour l'activite des variables.
	 */
	private double varInc = 1.0;

	protected ILits lits;

	private long nullchoice = 0;

	protected Heap heap;

	protected IPhaseSelectionStrategy phaseStrategy;

	public VarOrderHeap() {
		this(new RSATPhaseSelectionStrategy());
	}

	public VarOrderHeap(IPhaseSelectionStrategy strategy) {
		this.phaseStrategy = strategy;
	}

	/**
	 * Change the selection strategy.
	 * 
	 * @param strategy
	 */
	@Override
	public void setPhaseSelectionStrategy(IPhaseSelectionStrategy strategy) {
		this.phaseStrategy = strategy;
	}

	@Override
	public IPhaseSelectionStrategy getPhaseSelectionStrategy() {
		return this.phaseStrategy;
	}

	@Override
	public void setLits(ILits lits) {
		this.lits = lits;
	}

	/**
	 * Selectionne une nouvelle variable, non affectee, ayant l'activite la plus
	 * elevee.
	 * 
	 * @return Lit.UNDEFINED si aucune variable n'est trouvee
	 */
	@Override
	public int select() {
		while (!this.heap.empty()) {
			int var = this.heap.getmin();

			if (this.lits.isUnassigned(toInternal(var))) {
				if (this.activity[var] < 0.0001) {
					this.nullchoice++;
				}
				int next = this.phaseStrategy.select(var);
				this.phaseStrategy.assignLiteral(next);
				return next;
			}
		}
		return ILits.UNDEFINED;
	}

	/**
	 * Change la valeur de varDecay.
	 * 
	 * @param d
	 *            la nouvelle valeur de varDecay
	 */
	@Override
	public void setVarDecay(double d) {
		this.varDecay = d;
	}

	/**
	 * Methode appelee quand la variable x est desaffectee.
	 * 
	 * @param x
	 */
	@Override
	public void undo(int x) {
		if (!this.heap.inHeap(x)) {
			this.heap.insert(x);
		}
	}

	/**
	 * Appelee lorsque l'activite de la variable x a change.
	 * 
	 * @param p
	 *            a literal
	 */
	@Override
	public void updateVar(int p) {
		int var = var(p);
		updateActivity(var);
		this.phaseStrategy.updateVar(p);
		if (this.heap.inHeap(var)) {
			this.heap.increase(var);
		}
	}

	protected void updateActivity(final int var) {
		if ((this.activity[var] += this.varInc) > VAR_RESCALE_BOUND) {
			varRescaleActivity();
		}
	}

	/**
     * 
     */
	@Override
	public void varDecayActivity() {
		this.varInc *= this.varDecay;
	}

	/**
     * 
     */
	private void varRescaleActivity() {
		for (int i = 1; i < this.activity.length; i++) {
			this.activity[i] *= VAR_RESCALE_FACTOR;
		}
		this.varInc *= VAR_RESCALE_FACTOR;
	}

	@Override
	public double varActivity(int p) {
		return this.activity[var(p)];
	}

	/**
     * 
     */
	public int numberOfInterestingVariables() {
		int cpt = 0;
		for (int i = 1; i < this.activity.length; i++) {
			if (this.activity[i] > 1.0) {
				cpt++;
			}
		}
		return cpt;
	}

	/**
	 * that method has the responsability to initialize all arrays in the
	 * heuristics. PLEASE CALL super.init() IF YOU OVERRIDE THAT METHOD.
	 */
	@Override
	public void init() {
		int nlength = this.lits.nVars() + 1;
		this.activity = new double[nlength];
		this.phaseStrategy.init(nlength);
		this.activity[0] = -1;
		this.heap = new Heap(this.activity);
		this.heap.setBounds(nlength);
		for (int i = 1; i < nlength; i++) {
			assert i > 0;
			assert i <= this.lits.nVars() : "" + this.lits.nVars() + "/" + i; //$NON-NLS-1$ //$NON-NLS-2$
			this.activity[i] = 0.0;
			if (this.lits.belongsToPool(i)) {
				this.heap.insert(i);
			}
		}
	}

	@Override
	public String toString() {
		return "VSIDS like heuristics from MiniSAT using a heap " + this.phaseStrategy; //$NON-NLS-1$
	}

	public ILits getVocabulary() {
		return this.lits;
	}

	@Override
	public void printStat(PrintWriter out, String prefix) {
		out.println(prefix + "non guided choices\t" + this.nullchoice); //$NON-NLS-1$
	}

	@Override
	public void assignLiteral(int p) {
		// do nothing
	}

	@Override
	public void solutionFound() {
		// TODO Auto-generated method stub

	}
}
