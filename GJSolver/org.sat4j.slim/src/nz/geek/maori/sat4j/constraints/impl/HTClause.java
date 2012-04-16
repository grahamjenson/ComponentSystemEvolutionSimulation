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
package nz.geek.maori.sat4j.constraints.impl;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.neg;
import nz.geek.maori.sat4j.constraints.BinaryClause;
import nz.geek.maori.sat4j.constraints.Clause;
import nz.geek.maori.sat4j.constraints.UnitClause;
import nz.geek.maori.sat4j.core.Lits;
import nz.geek.maori.sat4j.core.UnitPropagationListener;
import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IVecInt;

/**
 * Lazy data structure for clause using the Head Tail data structure from SATO,
 * The original scheme is improved by avoiding moving pointers to literals but
 * moving the literals themselves.
 * 
 * We suppose here that the clause contains at least 3 literals. Use the
 * BinaryClause or UnaryClause clause data structures to deal with binary and
 * unit clauses.
 * 
 * @author leberre
 * @see BinaryClause
 * @see UnitClause
 * @since 2.1
 */
public class HTClause implements Clause {

	private static final long serialVersionUID = 1L;

	protected double activity;

	protected final int[] middleLits;

	protected ILits voc = null;

	protected int head;

	protected int tail;

	protected boolean learnt = false;

	/**
	 * Creates a new basic clause
	 * 
	 * @param voc
	 *            the vocabulary of the formula
	 * @param ps
	 *            A VecInt that WILL BE EMPTY after calling that method.
	 */
	public HTClause(IVecInt ps) {
		assert ps.size() > 1;
		this.head = ps.get(0);
		this.tail = ps.last();
		final int size = ps.size() - 2;
		assert size > 0;
		this.middleLits = new int[size];
		System.arraycopy(ps.toArray(), 1, this.middleLits, 0, size);
		ps.clear();
		assert ps.size() == 0;
		this.activity = 0;
	}

	@Override
	public void setVocabulary(ILits voc) {
		this.voc = voc;
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
		final int[] mylits = this.middleLits;
		for (int mylit : mylits) {
			if (this.voc.isFalsified(mylit)) {
				outReason.push(neg(mylit));
			}
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
		for (int middleLit : this.middleLits) {
			if (this.voc.isSatisfied(middleLit)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean propagate(UnitPropagationListener s, int p) {

		if (this.head == neg(p)) {
			final int[] mylits = this.middleLits;
			int temphead = 0;
			// moving head on the right
			while ((temphead < mylits.length)
					&& this.voc.isFalsified(mylits[temphead])) {
				temphead++;
			}
			assert temphead <= mylits.length;
			if (temphead == mylits.length) {
				this.voc.watch(p, this);
				return s.enqueue(this.tail, this);
			}
			this.head = mylits[temphead];
			mylits[temphead] = neg(p);
			this.voc.watch(neg(this.head), this);
			return true;
		}
		assert this.tail == neg(p);
		final int[] mylits = this.middleLits;
		int temptail = mylits.length - 1;
		// moving tail on the left
		while ((temptail >= 0) && this.voc.isFalsified(mylits[temptail])) {
			temptail--;
		}
		assert -1 <= temptail;
		if (-1 == temptail) {
			this.voc.watch(p, this);
			return s.enqueue(this.head, this);
		}
		this.tail = mylits[temptail];
		mylits[temptail] = neg(p);
		this.voc.watch(neg(this.tail), this);
		return true;
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
		stb.append(Lits.toString(this.head));
		stb.append("["); //$NON-NLS-1$
		if (this.voc != null) {
			stb.append(this.voc.valueToString(this.head));
		}
		stb.append("]"); //$NON-NLS-1$
		stb.append(" "); //$NON-NLS-1$
		for (int middleLit : this.middleLits) {
			stb.append(Lits.toString(middleLit));
			stb.append("["); //$NON-NLS-1$
			if (this.voc != null) {
				stb.append(this.voc.valueToString(middleLit));
			}
			stb.append("]"); //$NON-NLS-1$
			stb.append(" "); //$NON-NLS-1$
		}
		stb.append(Lits.toString(this.tail));
		stb.append("["); //$NON-NLS-1$
		if (this.voc != null) {
			stb.append(this.voc.valueToString(this.tail));
		}
		stb.append("]"); //$NON-NLS-1$
		return stb.toString();
	}

	/**
	 * Return the ith literal of the clause. Note that the order of the literals
	 * does change during the search...
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
		if (i == this.middleLits.length + 1) {
			return this.tail;
		}
		return this.middleLits[i - 1];
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
		return this.middleLits.length + 2;
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
		int[] tmp = new int[size()];
		System.arraycopy(this.middleLits, 0, tmp, 1, this.middleLits.length);
		tmp[0] = this.head;
		tmp[tmp.length - 1] = this.tail;
		return tmp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		try {
			HTClause wcl = (HTClause) obj;
			if ((wcl.head != this.head) || (wcl.tail != this.tail)) {
				return false;
			}
			if (this.middleLits.length != wcl.middleLits.length) {
				return false;
			}
			boolean ok;
			for (int lit : this.middleLits) {
				ok = false;
				for (int lit2 : wcl.middleLits) {
					if (lit == lit2) {
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
		long sum = this.head + this.tail;
		for (int p : this.middleLits) {
			sum += p;
		}
		return (int) sum / this.middleLits.length;
	}

	@Override
	public boolean learnt() {
		return learnt;
	}

	@Override
	public void incActivity(double claInc) {
		this.activity += claInc;

	}

	@Override
	public void setLearnt() {
		this.learnt = true;

	}

	@Override
	public void register() {
		// looking for the literal to put in tail
		// Only necessary if done during a seach so only learnt constraints need
		// this process
		// Assigns the literal that has the highest level to the tail
		if ((this.middleLits.length > 0) && this.learnt) {
			int maxi = 0;
			int maxlevel = this.voc.getLevel(this.middleLits[0]);
			for (int i = 1; i < this.middleLits.length; i++) {
				int level = this.voc.getLevel(this.middleLits[i]);
				if (level > maxlevel) {
					maxi = i;
					maxlevel = level;
				}
			}
			if (maxlevel > this.voc.getLevel(this.tail)) {
				int l = this.tail;
				this.tail = this.middleLits[maxi];
				this.middleLits[maxi] = l;
			}
		}
		// attach both head and tail literals.
		this.voc.watch(neg(this.head), this);
		this.voc.watch(neg(this.tail), this);

	}
}
