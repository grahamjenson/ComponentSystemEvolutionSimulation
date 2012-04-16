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
 * The reason simplification methods are coming from MiniSAT 1.14 released under 
 * the MIT license:
 * MiniSat -- Copyright (c) 2003-2005, Niklas Een, Niklas Sorensson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 *******************************************************************************/
package nz.geek.maori.sat4j.core;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.toDimacs;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;




import nz.geek.maori.sat4j.constraints.UnitClause;
import nz.geek.maori.sat4j.constraints.factory.ConstraintFactory;
import nz.geek.maori.sat4j.specs.IConstr;
import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IOrder;
import nz.geek.maori.sat4j.specs.ISolver;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.IVecInt;
import nz.geek.maori.sat4j.specs.Lbool;
import nz.geek.maori.sat4j.specs.SearchListener;
import nz.geek.maori.sat4j.tools.LiteralsUtils;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.var;
/**
 * The backbone of the library providing the modular implementation of a MiniSAT
 * 
 * @author leberre
 */
public class Solver implements ISolver, UnitPropagationListener,
VarActivityListener {

	private static final double CLAUSE_RESCALE_FACTOR = 1e-20;

	// Constraints
	private final IVec<IConstr> constrs = new Vec<IConstr>(); // Constr

	// Learnt Constrints
	private final IVec<IConstr> learnts = new Vec<IConstr>(); // Clause

	// TODO
	private double claInc = 1.0;

	// TODO
	private double claDecay = 1.0;

	// head of the queue in trail
	private int qhead = 0;

	// Trail
	protected final IVecInt trail = new VecInt(); // lit

	// decision Level
	protected final IVecInt level = new VecInt(); // int

	// rootLevel
	protected int rootLevel;

	private int[] model = null;

	protected ILits voc;

	private IOrder order;

	public void setOrder(IOrder order) {
		this.order = order;
		order.setLits(this.voc);
	}

	public IOrder getOrder()
	{
		return order;
	}

	private SolverStats stats = new SolverStats();

	private LearningStrategy learner;

	private volatile boolean undertimeout;

	private long conflictStopCount = Integer.MAX_VALUE;

	private SearchParams params;

	private SearchListener slistener = new SearchListener.EmptySearchListener();

	private RestartStrategy restarter;

	private boolean isDBSimplificationAllowed = false;

	public IVec<IConstr> getLearnts()
	{
		return learnts;
	}

	// Literals Learnt through the search
	private final IVecInt learnedLiterals = new VecInt();

	public IVecInt getLearnedLiterals() {
		return learnedLiterals;
	}

	/**
	 * creates a Solver without LearningListener. A learningListener must be
	 * added to the solver, else it won't backtrack!!! A data structure factory
	 * must be provided, else it won't work either.
	 */

	public Solver(LearningStrategy learner, IOrder order,
			RestartStrategy restarter) {
		this(learner, new SearchParams(), order, restarter);
	}

	public Solver(LearningStrategy learner, SearchParams params, IOrder order,
			RestartStrategy restarter) {
		this.learner = learner;
		this.order = order;
		this.voc = new Lits();
		this.order.setLits(this.voc);

		this.params = params;
		this.restarter = restarter;
	}

	public RestartStrategy getRestarter() {
		return restarter;
	}

	public void setRestarter(RestartStrategy restarter) {
		this.restarter = restarter;
	}

	@Override
	public void setSearchListener(SearchListener sl) {
		this.slistener = sl;
	}

	@Override
	public void setConflictsStopCount(int count) {
		this.conflictStopCount = count;
	}

	@Override
	public void expireTimeout() {
		this.undertimeout = false;
	}

	@Override
	public IVec<IConstr> getConstraints() {
		return this.constrs;
	}

	@Override
	public int nConstraints() {
		return this.constrs.size() + this.trail.size();
	}

	public void learn(IConstr c) {
		this.learnts.push(c);
		c.setLearnt();
		c.setVocabulary(getVocabulary());
		c.register();
		// this.addConstr(c);
		this.stats.learnedclauses++;
		switch (c.size()) {
		case 2:
			this.stats.learnedbinaryclauses++;
			break;
		case 3:
			this.stats.learnedternaryclauses++;
			break;
		default:
			// do nothing
		}
	}

	public final int decisionLevel() {
		return this.level.size();
	}

	@Override
	public boolean removeConstr(IConstr co) {
		if (co == null) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, "Null constraint tried to be removed");
			return true;
		}
		if (co instanceof UnitClause) {
			this.unitClauses.remove(co);

		}
		while (!this.trail.isEmpty()) {
			undoOne();
		}

		co.remove(this);
		this.constrs.remove(co);
		clearLearntClauses();
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean simplifyDB() {
		// Simplifie la base de clauses apres la premiere propagation des
		// clauses unitaires
		IVec<IConstr>[] cs = new IVec[] { this.constrs, this.learnts };
		for (int type = 0; type < 2; type++) {
			int j = 0;
			for (int i = 0; i < cs[type].size(); i++) {
				if (cs[type].get(i).simplify()) {
					cs[type].get(i).remove(this);
				} else {
					cs[type].moveTo(j++, i);
				}
			}
			cs[type].shrinkTo(j);
		}

		return true;
	}

	@Override
	public int[] model() {
		if (this.model == null) {
			return null;
		}
		int[] nmodel = new int[this.model.length];
		System.arraycopy(this.model, 0, nmodel, 0, this.model.length);
		return nmodel;
	}

	@Override
	public boolean enqueue(int p) {
		return enqueue(p, null);
	}

	/**
	 * Put the literal on the queue of assignments to be done.
	 * 
	 * @param p
	 *            the literal.
	 * @param from
	 *            the reason to propagate that literal, else null
	 * @return true if the assignment can be made, false if a conflict is
	 *         detected.
	 */
	@Override
	public boolean enqueue(int p, IConstr from) {
		assert p > 1;

		// if(from != null)
		// {
		// VecInt outReason = new VecInt();
		// from.calcReason(p, outReason);
		// counterConstr.push(ConstraintFactory.getInstance().createConstraint(outReason));
		// }
		//
		if (this.voc.isSatisfied(p)) {
			// literal is already satisfied. Skipping.
			return true;
		}
		if (this.voc.isFalsified(p)) {
			// conflicting enqueued assignment
			return false;
		}
		// new fact, store it
		this.voc.satisfies(p);
		this.voc.setLevel(p, decisionLevel());
		this.voc.setReason(p, from);
		this.trail.push(p);
		return true;
	}

	private boolean[] mseen = new boolean[0];

	private final IVecInt mpreason = new VecInt();

	private final IVecInt moutLearnt = new VecInt();

	public void analyze(IConstr confl, Pair results) {

		assert confl != null;

		final boolean[] seen = this.mseen;
		final IVecInt outLearnt = this.moutLearnt;
		final IVecInt preason = this.mpreason;

		outLearnt.clear();
		assert outLearnt.size() == 0;
		for (int i = 0; i < seen.length; i++) {
			seen[i] = false;
		}

		int counter = 0;
		int p = ILits.UNDEFINED;

		outLearnt.push(ILits.UNDEFINED);
		// reserve de la place pour le litteral falsifie
		int outBtlevel = 0;

		do {
			preason.clear();
			assert confl != null;
			confl.calcReason(p, preason);
			this.learnedConstraintsDeletionStrategy.onConflictAnalysis(confl);
			// Trace reason for p
			for (int j = 0; j < preason.size(); j++) {
				int q = preason.get(j);
				this.order.updateVar(q);
				if (!seen[q >> 1]) {
					seen[q >> 1] = true;
					if (this.voc.getLevel(q) == decisionLevel()) {
						counter++;
					} else if (this.voc.getLevel(q) > 0) {
						// only literals assigned after decision level 0 part of
						// the explanation
						outLearnt.push(q ^ 1);
						outBtlevel = Math.max(outBtlevel, this.voc.getLevel(q));
					}
				}
			}

			// select next reason to look at
			do {
				p = this.trail.last();
				confl = this.voc.getReason(p);
				undoOne();
			} while (!seen[p >> 1]);
			// seen[p.var] indique que p se trouve dans outLearnt ou dans
			// le dernier niveau de d?cision
		} while (--counter > 0);

		outLearnt.set(0, p ^ 1);
		int s1 = outLearnt.size();
		simpleSimplification(outLearnt);

		IConstr c = ConstraintFactory.getInstance().createConstraint(outLearnt);

		this.slistener.learn(c);
		this.learnedConstraintsDeletionStrategy.onConflict(c);
		results.reason = c;

		assert outBtlevel > -1;
		results.backtrackLevel = outBtlevel;
	}


	private final IVecInt analyzetoclear = new VecInt();

	private final IVecInt analyzestack = new VecInt();


	private void expensiveSimplification(IVecInt conflictToReduce) {
		// Simplify conflict clause (a lot):
		//
		int i, j;
		// (maintain an abstraction of levels involved in conflict)
		analyzetoclear.clear();
		conflictToReduce.copyTo(analyzetoclear);
		for (i = 1, j = 1; i < conflictToReduce.size(); i++)
			if (voc.getReason(conflictToReduce.get(i)) == null
					|| !analyzeRemovable(conflictToReduce.get(i)))
				conflictToReduce.moveTo(j++, i);
		conflictToReduce.shrink(i - j);
		stats.reducedliterals += (i - j);
	}

	// Check if 'p' can be removed.' min_level' is used to abort early if
	// visiting literals at a level that cannot be removed.
	//
	private boolean analyzeRemovable(int p) {
		assert voc.getReason(p) != null;
		ILits lvoc = voc;
		IVecInt lanalyzestack = analyzestack;
		IVecInt lanalyzetoclear = analyzetoclear;
		lanalyzestack.clear();
		lanalyzestack.push(p);
		final boolean[] seen = mseen;
		int top = lanalyzetoclear.size();
		while (lanalyzestack.size() > 0) {
			int q = lanalyzestack.last();
			assert lvoc.getReason(q) != null;
			IConstr c = lvoc.getReason(q);
			lanalyzestack.pop();
			for (int i = 0; i < c.size(); i++) {
				int l = c.get(i);
				if (lvoc.isFalsified(l) && !seen[var(l)]
				                                 && lvoc.getLevel(l) != 0) {
					if (lvoc.getReason(l) == null) {
						for (int j = top; j < lanalyzetoclear.size(); j++)
							seen[lanalyzetoclear.get(j) >> 1] = false;
						lanalyzetoclear.shrink(lanalyzetoclear.size() - top);
						return false;
					}
					seen[l >> 1] = true;
					lanalyzestack.push(l);
					lanalyzetoclear.push(l);
				}
			}
		}

		return true;
	}

	private void simpleSimplification(IVecInt conflictToReduce) {
		int i, j;
		final boolean[] seen = mseen;
		for (i = j = 1; i < conflictToReduce.size(); i++) {
			IConstr r = voc.getReason(conflictToReduce.get(i));
			if (r == null) {
				conflictToReduce.moveTo(j++, i);
			} else {
				for (int k = 0; k < r.size(); k++)
					if (voc.isFalsified(r.get(k)) && !seen[r.get(k) >> 1]
					&& (voc.getLevel(r.get(k)) != 0)) {
						conflictToReduce.moveTo(j++, i);
						break;
					}
			}
		}
		conflictToReduce.shrink(i - j);
		stats.reducedliterals += (i - j);
	}


	protected void undoOne() {
		// recupere le dernier litteral affecte
		int p = this.trail.last();
		assert p > 1;
		assert this.voc.getLevel(p) >= 0;
		int x = p >> 1;
		// desaffecte la variable
		this.voc.unassign(p);
		this.voc.setReason(p, null);
		this.voc.setLevel(p, -1);
		// met a jour l'heuristique
		this.order.undo(x);
		// depile le litteral des affectations
		this.trail.pop();
		// met a jour les contraintes apres desaffectation du litteral :
		// normalement, il n'y a rien a faire ici pour les prouveurs de type
		// Chaff??
		IVec<Undoable> undos = this.voc.undos(p);
		assert undos != null;
		while (undos.size() > 0) {
			undos.last().undo(p);
			undos.pop();
		}
	}

	@Override
	public void varBumpActivity(int p) {
		this.order.updateVar(p);
	}

	private final IVec<Propagatable> watched = new Vec<Propagatable>();

	/**
	 * @return null if not conflict is found, else a conflicting constraint.
	 */
	public IConstr propagate() {
		while (this.qhead < this.trail.size()) {
			this.stats.propagations++;
			int p = this.trail.get(this.qhead++);
			this.slistener.propagating(toDimacs(p), null);
			this.order.assignLiteral(p);
			// p is the literal to propagate
			// Moved original MiniSAT code to dsfactory to avoid
			// watches manipulation in counter Based clauses for instance.
			assert p > 1;
			this.watched.clear();
			IVec<Propagatable> watches = this.voc.watches(p);
			watches.moveTo(this.watched);
			final int size = this.watched.size();
			for (int i = 0; i < size; i++) {
				this.stats.inspects++;
				// try shortcut
				// shortcut = shortcuts.get(i);
				// if (shortcut != ILits.UNDEFINED && voc.isSatisfied(shortcut))
				// {
				// voc.watch(p, watched.get(i), shortcut);
				// stats.shortcuts++;
				// continue;
				// }
				if (!this.watched.get(i).propagate(this, p)) {
					// Constraint is conflicting: copy remaining watches to
					// watches[p]
					// and return constraint
					for (int j = i + 1; j < this.watched.size(); j++) {
						this.voc.watch(p, this.watched.get(j));
					}
					this.qhead = this.trail.size(); // propQ.clear();
					// FIXME enlever le transtypage
					return (IConstr) this.watched.get(i);
				}
			}
		}
		return null;
	}

	void record(IConstr constr) {
		constr.assertConstraint(this);
		this.slistener.adding(toDimacs(constr.get(0)));
		if (constr.size() == 1) {
			//			learnedLiterals.push(constr.getLits()[0]);
			//			System.out.println(constr +  " : " + learnedLiterals.size());

			this.stats.learnedliterals++;
		} else {
			this.learner.learns(constr);
		}
	}


	/**
	 * @return false ssi conflit imm?diat.
	 */
	public boolean assume(int p) {
		// Precondition: assume propagation queue is empty
		this.level.push(this.trail.size());
		return enqueue(p);
	}

	/**
	 * Revert to the state before the last push()
	 */
	private void cancel() {
		// assert trail.size() == qhead || !undertimeout;
		int decisionvar = this.trail.unsafeGet(this.level.last());
		this.slistener.backtracking(toDimacs(decisionvar));
		for (int c = this.trail.size() - this.level.last(); c > 0; c--) {
			undoOne();
		}
		this.level.pop();
	}

	/**
	 * Restore literals
	 */
	private void cancelLearntLiterals(int learnedLiteralsLimit) {
		this.learnedLiterals.clear();
		// assert trail.size() == qhead || !undertimeout;
		while (this.trail.size() > learnedLiteralsLimit) {
			this.learnedLiterals.push(this.trail.last());
			undoOne();
		}
		this.qhead = this.trail.size();
		// learnedLiterals = 0;
	}

	/**
	 * Cancel several levels of assumptions
	 * 
	 * @param level
	 */
	protected void cancelUntil(int level) {
		while (decisionLevel() > level) {
			cancel();
		}
		this.qhead = this.trail.size();
	}

	private final Pair analysisResult = new Pair();

	int i = 0;

	Lbool search(long nofConflicts) {
		assert this.rootLevel == decisionLevel();
		this.stats.starts++;
		int conflictC = 0;
		int backjumpLevel;

		// varDecay = 1 / params.varDecay;
		this.order.setVarDecay(1 / this.params.getVarDecay());
		this.claDecay = 1 / this.params.getClaDecay();

		do {
			this.i++;
			this.slistener.beginLoop();
			// propage les clauses unitaires
			IConstr confl = propagate();
			assert this.trail.size() == this.qhead;

			if (confl == null) {
				// No conflict found
				// simpliFYDB() prevents a correct use of
				// constraints removal.
				if ((decisionLevel() == 0) && this.isDBSimplificationAllowed) {
					// // Simplify the set of problem clause
					// // iff rootLevel==0
					this.stats.rootSimplifications++;
					boolean ret = simplifyDB();
					assert ret;
				}
				// was learnts.size() - nAssigns() > nofLearnts
				// if (nofLearnts.obj >= 0 && learnts.size() > nofLearnts.obj) {
				if (this.trail.size() == this.voc.realnVars()) {
					this.slistener.solutionFound();
					this.order.solutionFound();
					modelFound();
					return Lbool.TRUE;
				}
				if (conflictC >= nofConflicts) {
					// Reached bound on number of conflicts
					// Force a restart
					cancelUntil(this.rootLevel);
					return Lbool.UNDEFINED;
				}
				if (this.needToReduceDB) {
					reduceDB();
					this.needToReduceDB = false;
					// Runtime.getRuntime().gc();
				}
				// New variable decision
				this.stats.decisions++;
				int p = this.order.select();
				assert p > 1;
				this.slistener.assuming(toDimacs(p));
				boolean ret = assume(p);
				assert ret;
			} else {
				// un conflit apparait
				this.stats.conflicts++;
				conflictC++;
				this.slistener.conflictFound(confl, decisionLevel(),
						this.trail.size());
				this.conflictCount.newConflict();

				if (decisionLevel() == this.rootLevel) {
					// on est a la racine, la formule est inconsistante
					return Lbool.FALSE;
				}
				// analyze conflict
				// System.out.println(i);
				analyze(confl, this.analysisResult);
				assert this.analysisResult.backtrackLevel < decisionLevel();
				backjumpLevel = Math.max(this.analysisResult.backtrackLevel,
						this.rootLevel);
				this.slistener.backjump(backjumpLevel);
				cancelUntil(backjumpLevel);
				if (backjumpLevel == this.rootLevel) {
					conflictC = 0;
				}
				assert (decisionLevel() >= this.rootLevel)
				&& (decisionLevel() >= this.analysisResult.backtrackLevel);
				if (this.analysisResult.reason == null) {
					return Lbool.FALSE;
				}
				record(this.analysisResult.reason);
				this.analysisResult.reason = null;
				decayActivities();
			}
		} while (this.undertimeout);
		return Lbool.UNDEFINED; // timeout occured
	}

	protected void analyzeAtRootLevel(IConstr conflict) {
	}

	/**
	 * 
	 */
	void modelFound() {
		this.model = new int[this.trail.size()];
		int index = 0;
		for (int i = 1; i <= this.voc.nVars(); i++) {
			if (this.voc.belongsToPool(i)) {
				int p = this.voc.getFromPool(i);
				if (!this.voc.isUnassigned(p)) {
					this.model[index++] = this.voc.isSatisfied(p) ? p
							: LiteralsUtils.neg(p);
				}
			}
		}
		assert index == this.model.length;
		cancelUntil(this.rootLevel);
	}

	@Override
	public void clearLearntClauses() {
		for (Iterator<IConstr> iterator = this.learnts.iterator(); iterator
		.hasNext();) {
			iterator.next().remove(this);
		}
		this.learnts.clear();
		this.learnedLiterals.clear();
	}

	public void reduceDB() {
		this.stats.reduceddb++;
		this.learnedConstraintsDeletionStrategy.reduce();
		System.gc();
	}

	private final Comparator<IConstr> comparator = new Comparator<IConstr>() {
		@Override
		public int compare(IConstr c1, IConstr c2) {
			long delta = Math.round(c1.getActivity() - c2.getActivity());
			if (delta == 0) {
				return c1.size() - c2.size();
			}
			return (int) delta;
		}
	};

	private void sortOnActivity() {
		this.learnts.sort(this.comparator);
	}

	/**
	 * 
	 */
	protected void decayActivities() {
		this.order.varDecayActivity();
		claDecayActivity();
	}

	/**
	 * 
	 */
	private void claDecayActivity() {
		this.claInc *= this.claDecay;
	}

	private boolean needToReduceDB;

	private ConflictTimer conflictCount;

	interface LearnedConstraintsDeletionStrategy extends Serializable {

		void init();

		ConflictTimer getTimer();

		void reduce();

		void onConflict(IConstr outLearnt);

		void onConflictAnalysis(IConstr reason);
	}

	/**
	 * @since 2.1
	 */
	public final LearnedConstraintsDeletionStrategy glucose = new LearnedConstraintsDeletionStrategy() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int[] flags = new int[0];
		private int flag = 0;
		private int wall = 0;

		private final ConflictTimer clauseManagement = new ConflictTimerAdapter(10000) {
			private static final long serialVersionUID = 1L;
			private int nbconflict = 0;
			private static final int MAX_CLAUSE = 5000;
			private static final int INC_CLAUSE = 1000;
			private int nextbound = MAX_CLAUSE;

			@Override
			void run() {
				this.nbconflict += bound();
				if (this.nbconflict >= this.nextbound) {
					this.nextbound += INC_CLAUSE;
					if (this.nextbound > wall) {
						this.nextbound = wall;
					}
					this.nbconflict = 0;
					Solver.this.needToReduceDB = true;
				}
			}
		};

		@Override
		public void reduce() {
			sortOnActivity();
			int i, j;
			for (i = j = (Solver.this.learnts.size()*3)/4; i < Solver.this.learnts.size(); i++) {
				IConstr c = Solver.this.learnts.get(i);
				if (c.locked() || (c.getActivity() <= 2 )) {
					Solver.this.learnts.set(j++, Solver.this.learnts.get(i));
					
				} else {
					c.remove(Solver.this);
				}
			}
			Solver.this.learnts.shrinkTo(j);
		}

		@Override
		public ConflictTimer getTimer() {
			return this.clauseManagement;
		}

		@Override
		public String toString() {
			return "Glucose learned constraints deletion strategy";
		}

		@Override
		public void init() {
			final int howmany = Solver.this.voc.nVars();
			this.wall = Solver.this.constrs.size() > 10000 ? Solver.this.constrs
					.size() : 10000;
					if (this.flags.length <= howmany) {
						this.flags = new int[howmany + 1];
					}

		}

		@Override
		public void onConflict(IConstr constr) {
			int nblevel = 1;
			this.flag++;
			int currentLevel;
			for (int i = 1; i < constr.size(); i++) {
				currentLevel = Solver.this.voc.getLevel(constr.get(i));
				if ((currentLevel > 0)
						&& (this.flags[currentLevel] != this.flag)) {
					this.flags[currentLevel] = this.flag;
					nblevel++;
				}
			}
			constr.incActivity(nblevel);
		}

		@Override
		public void onConflictAnalysis(IConstr reason) {
			// do nothing
		}
	};

	private LearnedConstraintsDeletionStrategy learnedConstraintsDeletionStrategy = this.glucose;

	public boolean isSatisfiable()
	{
		return isSatisfiable(new int[]{});
	}

	public boolean isSatisfiable(int[] assumps) {
		Lbool status = Lbool.UNDEFINED;
		this.undertimeout = true;
		final int howmany = this.voc.nVars();
		if (this.mseen.length <= howmany) {
			this.mseen = new boolean[howmany + 1];
		}
		this.trail.ensure(howmany);
		this.level.ensure(howmany);
		this.learnedLiterals.ensure(howmany);
		this.slistener.start();
		this.model = null; // forget about previous model
		this.order.init();
		this.learnedConstraintsDeletionStrategy.init();
		this.restarter.init(this.params);

		int learnedLiteralsLimit = this.trail.size();


		for (UnitClause uc : this.unitClauses) {
			if (!enqueue(uc.get(0), uc)) {
				return false;
			}
		}
		// push previously learned literals
		for (int i = 0; i < this.learnedLiterals.size(); i++) {
			enqueue(this.learnedLiterals.get(i));
		}

		//		simplifyDB();

		// propagate constraints
		IConstr confl = propagate();


		if (confl != null) {
			analyzeAtRootLevel(confl);
			this.slistener.conflictFound(confl, 0, 0);
			this.slistener.end(Lbool.FALSE);
			cancelUntil(0);
			cancelLearntLiterals(learnedLiteralsLimit);
			return false;
		}

		// push incremental assumptions
		this.rootLevel = decisionLevel();

		for(int a : assumps)
		{
			this.assume(a);
		}

		// moved initialization here if new literals are added in the
		// assumptions.
		// TODO check if this breaks it
		// order.init(); // duplicated on purpose
		this.learner.init();

		if (this.conflictCount == null) {
			ConflictTimer conflictTimeout = new ConflictTimerAdapter(
					(int) this.conflictStopCount) {

				@Override
				public void run() {
					Solver.this.undertimeout = false;
				}
			};
			this.conflictCount = new ConflictTimerContainer().add(
					conflictTimeout).add(
							this.learnedConstraintsDeletionStrategy.getTimer());
		}

		this.needToReduceDB = false;
		// Solve
		//	int reses = 0;
		while ((status == Lbool.UNDEFINED) && this.undertimeout) {
			status = search(this.restarter.nextRestartNumberOfConflict());
			if (status == Lbool.UNDEFINED) {


				this.restarter.onRestart();
				this.slistener.restarting();

			}
		}

		cancelUntil(0);
		cancelLearntLiterals(learnedLiteralsLimit);

		this.slistener.end(status);
		return status == Lbool.TRUE;
	}

	/**
	 * @since 2.1
	 */
	public void printLearntClausesInfos(PrintWriter out, String prefix) {
		Map<String, Counter> learntTypes = new HashMap<String, Counter>();
		for (Iterator<IConstr> it = this.learnts.iterator(); it.hasNext();) {
			String type = it.next().getClass().getName();
			Counter count = learntTypes.get(type);
			if (count == null) {
				learntTypes.put(type, new Counter());
			} else {
				count.inc();
			}
		}
		out.print(prefix);
		out.println("learnt constraints type ");
		for (Map.Entry<String, Counter> entry : learntTypes.entrySet()) {
			out.println(prefix + entry.getKey() + " => " + entry.getValue());
		}
	}

	@Override
	public SolverStats getStats() {
		return this.stats;
	}

	@Override
	public ILits getVocabulary() {
		return this.voc;
	}

	@Override
	public void reset() {
		this.trail.clear();
		this.level.clear();
		this.qhead = 0;
		for (Iterator<IConstr> iterator = this.constrs.iterator(); iterator
		.hasNext();) {
			iterator.next().remove(this);
		}
		this.constrs.clear();
		clearLearntClauses();
		this.voc.resetPool();
		this.stats.reset();
	}

	@Override
	public int nVars() {
		return this.voc.nVars();
	}

	@Override
	public void addConstrs(Collection<IConstr> cons) {
		for (IConstr con : cons) {
			addConstr(con);
		}
	}

	ArrayList<UnitClause> unitClauses = new ArrayList<UnitClause>();

	/**
	 * @param constr
	 *            a constraint implementing the Constr interface.
	 * @return
	 */
	@Override
	public void addConstr(IConstr constr) {
		// TODO Sanity Check
		// Clauses.sanityCheck(ps, voc, s)
		if (constr == null) {
			return;
		}

		this.constrs.push(constr);
		VecInt lits = new VecInt(constr.getLits());
		this.voc.initVariables(lits);

		if (constr instanceof UnitClause) {
			this.unitClauses.add((UnitClause) constr);

		} else {
			// New From Graham
			// By putting the registration here it enables me
			// to decouple the DSF from the Solver
			constr.setVocabulary(getVocabulary());

			constr.register();
		}
	}

	/**
	 * returns the ith constraint in the solver.
	 * 
	 * @param i
	 *            the constraint number (begins at 0)
	 * @return the ith constraint
	 */
	public IConstr getIthConstr(int i) {
		return this.constrs.get(i);
	}

	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();
		Object[] objs = { this.learner, this.params, this.order,
				this.restarter, this.learnedConstraintsDeletionStrategy };
		stb.append("--- Begin Solver configuration ---"); //$NON-NLS-1$
		stb.append("\n"); //$NON-NLS-1$
		for (Object o : objs) {
			stb.append(o.toString());
			stb.append("\n"); //$NON-NLS-1$
		}
		stb.append("timeout=");
		stb.append(this.conflictStopCount);
		stb.append(" conflicts\n");
		stb.append("DB Simplification allowed=");
		stb.append(this.isDBSimplificationAllowed);
		stb.append("\n");
		stb.append("--- End Solver configuration ---"); //$NON-NLS-1$
		return stb.toString();
	}

	@Override
	public boolean isDBSimplificationAllowed() {
		return this.isDBSimplificationAllowed;
	}

	@Override
	public void setDBSimplificationAllowed(boolean status) {
		this.isDBSimplificationAllowed = status;
	}

	/**
	 * @since 2.1
	 */
	@Override
	public void unset(int p) {
		int current = this.trail.last();
		while (current != p) {
			undoOne();
			current = this.trail.last();
		}
		undoOne();
	}

	public void removeConstrs(Collection<IConstr> toRemove) {
		for (IConstr tr : toRemove) {
			removeConstr(tr);
		}
	}

}

interface ConflictTimer {

	void reset();

	void newConflict();
}

abstract class ConflictTimerAdapter implements Serializable, ConflictTimer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int counter;

	private final int bound;

	ConflictTimerAdapter(final int bound) {
		this.bound = bound;
		this.counter = 0;
	}

	@Override
	public void reset() {
		this.counter = 0;
	}

	@Override
	public void newConflict() {
		this.counter++;
		if (this.counter == this.bound) {
			run();
			this.counter = 0;
		}
	}

	abstract void run();

	public int bound() {
		return this.bound;
	}
}

class ConflictTimerContainer implements Serializable, ConflictTimer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final IVec<ConflictTimer> timers = new Vec<ConflictTimer>();

	ConflictTimerContainer add(ConflictTimer timer) {
		this.timers.push(timer);
		return this;
	}

	@Override
	public void reset() {
		Iterator<ConflictTimer> it = this.timers.iterator();
		while (it.hasNext()) {
			it.next().reset();
		}
	}

	@Override
	public void newConflict() {
		Iterator<ConflictTimer> it = this.timers.iterator();
		while (it.hasNext()) {
			it.next().newConflict();
		}
	}
}
