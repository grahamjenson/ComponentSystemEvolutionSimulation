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
package nz.geek.maori.sat4j.constraints;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.neg;
import nz.geek.maori.sat4j.core.UnitPropagationListener;
import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IVecInt;
import nz.geek.maori.sat4j.tools.LiteralsUtils;

/**
 * Data structure for binary clause.
 * 
 * @author leberre
 * @since 2.1
 */
public class BinaryClause implements Clause {

	protected double activity;

	private ILits voc;

	protected boolean learnt = false;

	protected int head;

	protected int tail;

	/**
	 * Creates a new basic clause
	 * 
	 * @param voc
	 *            the vocabulary of the formula
	 * @param ps
	 *            A VecInt that WILL BE EMPTY after calling that method.
	 */
	public BinaryClause(IVecInt ps) {
		assert ps.size() == 2;
		this.head = ps.get(0);
		this.tail = ps.get(1);
		this.activity = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#calcReason(Solver, Lit, Vec)
	 */
	@Override
	public void calcReason(int p, IVecInt outReason) {
		if (this.voc.isFalsified(this.head)) {
			outReason.push(neg(this.head));
		}
		if (this.voc.isFalsified(this.tail)) {
			outReason.push(neg(this.tail));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#remove(Solver)
	 */
	@Override
	public void remove(UnitPropagationListener upl) {
		this.voc.removeWatch(neg(this.head), this);
		this.voc.removeWatch(neg(this.tail), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Constr#simplify(Solver)
	 */
	@Override
	public boolean simplify() {
		if (this.voc.isSatisfied(this.head) || this.voc.isSatisfied(this.tail)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean propagate(UnitPropagationListener s, int p) {
		this.voc.watch(p, this);
		if (this.head == neg(p)) {
			return s.enqueue(this.tail, this);
		}
		assert this.tail == neg(p);
		return s.enqueue(this.head, this);
	}

	/*
	 * For learnt clauses only @author leberre
	 */
	@Override
	public boolean locked() {
		return (this.voc.getReason(this.head) == this)
				|| (this.voc.getReason(this.tail) == this);
	}

	/**
	 * @return the activity of the clause
	 */
	@Override
	public double getActivity() {
		return this.activity;
	}

	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();
		stb.append(LiteralsUtils.toDimacs(this.head));
		stb.append("["); //$NON-NLS-1$
		if (this.voc != null) {
			stb.append(this.voc.valueToString(this.head));
		}
		stb.append("]"); //$NON-NLS-1$
		stb.append(" "); //$NON-NLS-1$
		stb.append(LiteralsUtils.toDimacs(this.tail));
		stb.append("["); //$NON-NLS-1$
		if (this.voc != null) {
			stb.append(this.voc.valueToString(this.tail));
		}
		stb.append("]"); //$NON-NLS-1$
		return stb.toString();
	}

	/**
	 * Retourne le ieme literal de la clause. Attention, cet ordre change durant
	 * la recherche.
	 * 
	 * @param i
	 *            the index of the literal
	 * @return the literal
	 */
	@Override
	public int get(int i) {
		if (i == 0) {
			return this.head;
		}
		assert i == 1;
		return this.tail;
	}

	/**
	 * @param d
	 */
	@Override
	public void rescaleBy(double d) {
		this.activity *= d;
	}

	@Override
	public int size() {
		return 2;
	}

	@Override
	public void assertConstraint(UnitPropagationListener s) {
		// assert voc.isUnassigned(head);
		boolean ret = s.enqueue(this.head, this);
		assert ret;
	}

	@Override
	public ILits getVocabulary() {
		return this.voc;
	}

	@Override
	public int[] getLits() {
		int[] tmp = new int[2];
		tmp[0] = this.head;
		tmp[1] = this.tail;
		return tmp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		try {
			BinaryClause wcl = (BinaryClause) obj;
			if ((wcl.head != this.head) || (wcl.tail != this.tail)) {
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		long sum = this.head + this.tail;
		return (int) sum / 2;
	}

	@Override
	public void register() {
		this.voc.watch(neg(this.head), this);
		this.voc.watch(neg(this.tail), this);
	}

	@Override
	public void incActivity(double claInc) {
		this.activity += claInc;
	}

	@Override
	public void setVocabulary(ILits voc) {
		this.voc = voc;

	}

	@Override
	public boolean learnt() {
		return this.learnt;
	}

	@Override
	public void setLearnt() {
		this.learnt = true;

	}
}
