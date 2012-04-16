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
 * Based on the pseudo boolean algorithms described in:
 * A fast pseudo-Boolean constraint solver Chai, D.; Kuehlmann, A.
 * Computer-Aided Design of Integrated Circuits and Systems, IEEE Transactions on
 * Volume 24, Issue 3, March 2005 Page(s): 305 - 317
 * 
 * and 
 * Heidi E. Dixon, 2004. Automating Pseudo-Boolean Inference within a DPLL 
 * Framework. Ph.D. Dissertation, University of Oregon.
 *******************************************************************************/
package nz.geek.maori.sat4j.constraints.impl;

import java.math.BigInteger;

import nz.geek.maori.sat4j.constraints.PBConstr;
import nz.geek.maori.sat4j.core.Lits;
import nz.geek.maori.sat4j.core.Undoable;
import nz.geek.maori.sat4j.core.UnitPropagationListener;
import nz.geek.maori.sat4j.specs.ContradictionException;
import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IVecInt;

/**
 * Data structure for pseudo-boolean constraint with watched literals.
 * 
 * All literals are watched. The sum of the literals satisfied or unvalued is
 * always memorized, to detect conflict.
 * 
 * @author anne
 * 
 */
public final class MaxWatchPb implements PBConstr, Undoable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constraint activity
	 */
	protected double activity;

	/**
	 * coefficients of the literals of the constraint
	 */
	protected BigInteger[] coefs;

	/**
	 * degree of the pseudo-boolean constraint
	 */
	protected BigInteger degree;

	/**
	 * literals of the constraint
	 */
	protected int[] lits;

	/**
	 * true if the constraint is a learned constraint
	 */
	protected boolean learnt = false;

	/**
	 * sum of the coefficients of the literals satisfied or unvalued
	 */
	private BigInteger watchCumul = BigInteger.ZERO;

	/**
	 * constraint's vocabulary
	 */
	protected ILits voc;

	/**
	 * Builds a PB constraint for a0.x0 + a1.x1 + ... + an.xn >= k
	 * 
	 * @param lits
	 *            literals of the constraint (x0,x1, ... xn)
	 * @param coefs
	 *            coefficients of the left side of the constraint (a0, a1, ...
	 *            an)
	 * @param degree
	 *            degree of the constraint (k)
	 */
	public MaxWatchPb(int[] lits, BigInteger[] coefs, BigInteger degree) {

		this.lits = lits;
		this.coefs = coefs;
		this.degree = degree;
		// arrays are sorted by decreasing coefficients
		sort();

		this.activity = 0;
		this.watchCumul = BigInteger.ZERO;
	}

	@Override
	public void setVocabulary(ILits voc) {
		this.voc = voc;
	}

	@Override
	public ILits getVocabulary() {
		return this.voc;
	}

	/**
	 * compute the reason for the assignment of a literal
	 * 
	 * @param p
	 *            a falsified literal (or Lit.UNDEFINED)
	 * @param outReason
	 *            list of falsified literals for which the negation is the
	 *            reason of the assignment
	 * @see nz.geek.maori.sat4j.specs.IConstr#calcReason(int, IVecInt)
	 */
	@Override
	public void calcReason(int p, IVecInt outReason) {
		for (int q : this.lits) {
			if (this.voc.isFalsified(q)) {
				outReason.push(q ^ 1);
			}
		}
	}

	/**
	 * to obtain the i-th literal of the constraint
	 * 
	 * @param i
	 *            index of the literal
	 * @return the literal
	 */
	@Override
	public int get(int i) {
		return this.lits[i];
	}

	/**
	 * to obtain the activity value of the constraint
	 * 
	 * @return activity value of the constraint
	 * @see nz.geek.maori.sat4j.specs.IConstr#getActivity()
	 */
	@Override
	public double getActivity() {
		return this.activity;
	}

	/**
	 * increase activity value of the constraint
	 * 
	 * @see nz.geek.maori.sat4j.specs.IConstr#incActivity(double)
	 */
	@Override
	public void incActivity(double claInc) {
		if (this.learnt) {
			this.activity += claInc;
		}
	}

	/**
	 * compute the sum of the coefficients of the satisfied or non-assigned
	 * literals of a described constraint (usually called poss)
	 * 
	 * @param coefs
	 *            coefficients of the constraint
	 * @return poss
	 */
	public BigInteger computeLeftSide(BigInteger[] theCoefs) {
		BigInteger poss = BigInteger.ZERO;
		// for each literal
		for (int i = 0; i < this.lits.length; i++) {
			if (!this.voc.isFalsified(this.lits[i])) {
				assert theCoefs[i].signum() >= 0;
				poss = poss.add(theCoefs[i]);
			}
		}
		return poss;
	}

	/**
	 * compute the sum of the coefficients of the satisfied or non-assigned
	 * literals of the current constraint (usually called poss)
	 * 
	 * @return poss
	 */
	public BigInteger computeLeftSide() {
		return computeLeftSide(this.coefs);
	}

	/**
	 * tests if the constraint is still satisfiable.
	 * 
	 * this method is only called in assertions.
	 * 
	 * @return the constraint is satisfiable
	 */
	protected boolean isSatisfiable() {
		return computeLeftSide().compareTo(this.degree) >= 0;
	}

	/**
	 * is the constraint a learnt constraint ?
	 * 
	 * @return true if the constraint is learnt, else false
	 * @see org.sat4j.specs.IConstr#learnt()
	 */
	@Override
	public boolean learnt() {
		return this.learnt;
	}

	/**
	 * The constraint is the reason of a unit propagation.
	 * 
	 * @return true
	 */
	@Override
	public boolean locked() {
		for (int p : this.lits) {
			if (this.voc.getReason(p) == this) {
				return true;
			}
		}
		return false;
	}

	/**
	 * to re-scale the activity of the constraint
	 * 
	 * @param d
	 *            adjusting factor
	 */
	@Override
	public void rescaleBy(double d) {
		this.activity *= d;
	}

	void selectionSort(int from, int to) {
		int i, j, best_i;
		BigInteger tmp;
		int tmp2;

		for (i = from; i < to - 1; i++) {
			best_i = i;
			for (j = i + 1; j < to; j++) {
				if ((this.coefs[j].compareTo(this.coefs[best_i]) > 0)
						|| ((this.coefs[j].equals(this.coefs[best_i])) && (this.lits[j] > this.lits[best_i]))) {
					best_i = j;
				}
			}
			tmp = this.coefs[i];
			this.coefs[i] = this.coefs[best_i];
			this.coefs[best_i] = tmp;
			tmp2 = this.lits[i];
			this.lits[i] = this.lits[best_i];
			this.lits[best_i] = tmp2;
		}
	}

	/**
	 * the constraint is learnt
	 */
	@Override
	public void setLearnt() {
		this.learnt = true;
	}

	/**
	 * simplify the constraint (if it is satisfied)
	 * 
	 * @return true if the constraint is satisfied, else false
	 */
	@Override
	public boolean simplify() {
		BigInteger cumul = BigInteger.ZERO;

		int i = 0;
		while ((i < this.lits.length) && (cumul.compareTo(this.degree) < 0)) {
			if (this.voc.isSatisfied(this.lits[i])) {
				// strong measure
				cumul = cumul.add(this.coefs[i]);
			}
			i++;
		}

		return (cumul.compareTo(this.degree) >= 0);
	}

	@Override
	public final int size() {
		return this.lits.length;
	}

	/**
	 * sort coefficient and literal arrays
	 */
	final protected void sort() {
		assert this.lits != null;
		if (this.coefs.length > 0) {
			this.sort(0, size());
			BigInteger buffInt = this.coefs[0];
			for (int i = 1; i < this.coefs.length; i++) {
				assert buffInt.compareTo(this.coefs[i]) >= 0;
				buffInt = this.coefs[i];
			}

		}
	}

	/**
	 * sort partially coefficient and literal arrays
	 * 
	 * @param from
	 *            index for the beginning of the sort
	 * @param to
	 *            index for the end of the sort
	 */
	final protected void sort(int from, int to) {
		int width = to - from;
		if (width <= 15) {
			selectionSort(from, to);
		} else {
			int indPivot = width / 2 + from;
			BigInteger pivot = this.coefs[indPivot];
			int litPivot = this.lits[indPivot];
			BigInteger tmp;
			int i = from - 1;
			int j = to;
			int tmp2;

			for (;;) {
				do {
					i++;
				} while ((this.coefs[i].compareTo(pivot) > 0)
						|| ((this.coefs[i].equals(pivot)) && (this.lits[i] > litPivot)));
				do {
					j--;
				} while ((pivot.compareTo(this.coefs[j]) > 0)
						|| ((this.coefs[j].equals(pivot)) && (this.lits[j] < litPivot)));

				if (i >= j) {
					break;
				}

				tmp = this.coefs[i];
				this.coefs[i] = this.coefs[j];
				this.coefs[j] = tmp;
				tmp2 = this.lits[i];
				this.lits[i] = this.lits[j];
				this.lits[j] = tmp2;
			}

			sort(from, i);
			sort(i, to);
		}

	}

	@Override
	public void assertConstraint(UnitPropagationListener s) {
		// Compute the slack of the constraint
		BigInteger tmp = computeLeftSide().subtract(this.degree);
		for (int i = 0; i < this.lits.length; i++) {
			if (this.voc.isUnassigned(this.lits[i])
					&& (tmp.compareTo(this.coefs[i]) < 0)) {
				boolean ret = s.enqueue(this.lits[i], this);
				assert ret;
			}
		}
	}

	/**
	 * @return Returns the degree.
	 */
	@Override
	public BigInteger getDegree() {
		return this.degree;
	}

	@Override
	public void register() {
		try {
			assert this.watchCumul.equals(BigInteger.ZERO);
			for (int i = 0; i < this.lits.length; i++) {
				if (this.voc.isFalsified(this.lits[i])) {
					if (this.learnt) {
						this.voc.undos(this.lits[i] ^ 1).push(this);
						this.voc.watch(this.lits[i] ^ 1, this);
					}
				} else {
					// updating of the initial value for the counter
					this.voc.watch(this.lits[i] ^ 1, this);
					this.watchCumul = this.watchCumul.add(this.coefs[i]);
				}
			}

			assert this.watchCumul.compareTo(computeLeftSide()) >= 0;
			if (!this.learnt && (this.watchCumul.compareTo(this.degree) < 0)) {
				throw new ContradictionException("non satisfiable constraint");
			}
		} catch (ContradictionException e) {
			assert false;
		}
	}

	/**
	 * to obtain the coefficients of the constraint.
	 * 
	 * @return a copy of the array of the coefficients
	 */
	@Override
	public BigInteger[] getCoefs() {
		BigInteger[] coefsBis = new BigInteger[this.coefs.length];
		System.arraycopy(this.coefs, 0, coefsBis, 0, this.coefs.length);
		return coefsBis;
	}

	/**
	 * to obtain the literals of the constraint.
	 * 
	 * @return a copy of the array of the literals
	 */
	@Override
	public int[] getLits() {
		int[] litsBis = new int[this.lits.length];
		System.arraycopy(this.lits, 0, litsBis, 0, this.lits.length);
		return litsBis;
	}

	/**
	 * Propagation of a falsified literal
	 * 
	 * @param s
	 *            the solver
	 * @param p
	 *            the propagated literal (it must be falsified)
	 * @return false iff there is a conflict
	 */
	@Override
	public boolean propagate(UnitPropagationListener s, int p) {
		this.voc.watch(p, this);

		assert this.watchCumul.compareTo(computeLeftSide()) >= 0 : ""
				+ this.watchCumul + "/" + computeLeftSide() + ":" + this.learnt;

		// finding the index for p in the array of literals
		int indiceP = 0;
		while ((this.lits[indiceP] ^ 1) != p) {
			indiceP++;
		}

		// compute the new value for watchCumul
		BigInteger coefP = this.coefs[indiceP];
		BigInteger newcumul = this.watchCumul.subtract(coefP);

		if (newcumul.compareTo(this.degree) < 0) {
			// there is a conflict
			assert !isSatisfiable();
			return false;
		}

		// if no conflict, not(p) can be propagated
		// allow a later un-assignation
		this.voc.undos(p).push(this);
		// really update watchCumul
		this.watchCumul = newcumul;

		// propagation
		int ind = 0;
		// limit is the margin between the sum of the coefficients of the
		// satisfied+unassigned literals
		// and the degree of the constraint
		BigInteger limit = this.watchCumul.subtract(this.degree);
		// for each coefficient greater than limit
		while ((ind < this.coefs.length)
				&& (limit.compareTo(this.coefs[ind]) < 0)) {
			// its corresponding literal is implied
			if (this.voc.isUnassigned(this.lits[ind])
					&& (!s.enqueue(this.lits[ind], this))) {
				// if it is not possible then there is a conflict
				assert !isSatisfiable();
				return false;
			}
			ind++;
		}

		assert this.learnt
				|| (this.watchCumul.compareTo(computeLeftSide()) >= 0);
		assert this.watchCumul.compareTo(computeLeftSide()) >= 0;
		return true;
	}

	/**
	 * Remove a constraint from the solver
	 */
	@Override
	public void remove(UnitPropagationListener upl) {
		for (int i = 0; i < this.lits.length; i++) {
			if (!this.voc.isFalsified(this.lits[i])) {
				this.voc.removeWatch(this.lits[i] ^ 1, this);
			}
		}
	}

	/**
	 * this method is called during backtrack
	 * 
	 * @param p
	 *            an unassigned literal
	 */
	@Override
	public void undo(int p) {
		int indiceP = 0;
		while ((this.lits[indiceP] ^ 1) != p) {
			indiceP++;
		}

		// assert coefs[indiceP].signum() > 0;

		this.watchCumul = this.watchCumul.add(this.coefs[indiceP]);
	}

	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();

		if (this.lits.length > 0) {
			for (int i = 0; i < this.lits.length; i++) {
				// if (voc.isUnassigned(lits[i])) {
				stb.append(" + ");
				stb.append(this.coefs[i]);
				stb.append(".");
				stb.append(Lits.toString(this.lits[i]));
				if (this.voc != null) {
					stb.append("[");
					stb.append(this.voc.valueToString(this.lits[i]));
					stb.append("@");
					stb.append(this.voc.getLevel(this.lits[i]));
					stb.append("]");
				}
				stb.append(" ");
				// }
			}
			stb.append(">= ");
			stb.append(this.degree);
		}
		return stb.toString();
	}

	@Override
	public boolean equals(Object pb) {
		if (pb == null) {
			return false;
		}
		// this method should be simplified since now two constraints should
		// have
		// always
		// their literals in the same order
		try {

			MaxWatchPb wpb = (MaxWatchPb) pb;
			if (!this.degree.equals(wpb.degree)
					|| (this.coefs.length != wpb.coefs.length)
					|| (this.lits.length != wpb.lits.length)) {
				return false;
			}
			int lit;
			boolean ok;
			for (int ilit = 0; ilit < this.coefs.length; ilit++) {
				lit = this.lits[ilit];
				ok = false;
				for (int ilit2 = 0; ilit2 < this.coefs.length; ilit2++) {
					if (wpb.lits[ilit2] == lit) {
						if (!wpb.coefs[ilit2].equals(this.coefs[ilit])) {
							return false;
						}

						ok = true;
						break;

					}
				}
				if (!ok) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		long sum = 0;
		for (int p : this.lits) {
			sum += p;
		}
		return (int) sum / this.lits.length;
	}

}
