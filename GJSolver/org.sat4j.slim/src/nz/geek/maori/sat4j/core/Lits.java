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
package nz.geek.maori.sat4j.core;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.toDimacs;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.toInternal;
import nz.geek.maori.sat4j.specs.IConstr;
import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.IVecInt;
import nz.geek.maori.sat4j.tools.LiteralsUtils;

/**
 * @author laihem
 * @author leberre
 * 
 */
public final class Lits implements ILits {

	private static final int DEFAULT_INIT_SIZE = 128;

	private boolean pool[] = new boolean[1];

	private int realnVars = 0;

	@SuppressWarnings("unchecked")
	private IVec<Propagatable>[] watches = new IVec[0];

	private int[] level = new int[0];

	private IConstr[] reason = new IConstr[0];

	private int maxvarid = 0;

	@SuppressWarnings("unchecked")
	private IVec<Undoable>[] undos = new IVec[0];

	private boolean[] falsified = new boolean[0];

	public Lits() {
		init(DEFAULT_INIT_SIZE);
	}

	@SuppressWarnings({ "unchecked" })
	private final void init(int nvar) {
		if (nvar < this.pool.length) {
			return;
		}
		assert nvar >= 0;
		// let some space for unused 0 indexer.
		int nvars = nvar + 1;
		boolean[] npool = new boolean[nvars];
		System.arraycopy(this.pool, 0, npool, 0, this.pool.length);
		this.pool = npool;

		int[] nlevel = new int[nvars];
		System.arraycopy(this.level, 0, nlevel, 0, this.level.length);
		this.level = nlevel;

		IVec<Propagatable>[] nwatches = new IVec[2 * nvars];
		System.arraycopy(this.watches, 0, nwatches, 0, this.watches.length);
		this.watches = nwatches;

		IVec<Undoable>[] nundos = new IVec[nvars];
		System.arraycopy(this.undos, 0, nundos, 0, this.undos.length);
		this.undos = nundos;

		IConstr[] nreason = new IConstr[nvars];
		System.arraycopy(this.reason, 0, nreason, 0, this.reason.length);
		this.reason = nreason;

		boolean[] newFalsified = new boolean[2 * nvars];
		System.arraycopy(this.falsified, 0, newFalsified, 0,
				this.falsified.length);
		this.falsified = newFalsified;
	}

	@Override
	public int getFromPool(int x) {
		int var = Math.abs(x);
		if (var >= this.pool.length) {
			// pool.length << 1 = pool.length * 2
			init(Math.max(var, this.pool.length << 1));
		}
		assert var < this.pool.length;
		if (var > this.maxvarid) {
			this.maxvarid = var;
		}
		int lit = LiteralsUtils.toInternal(x);
		assert lit > 1;
		if (!this.pool[var]) {
			this.realnVars++;
			this.pool[var] = true;
			this.watches[var << 1] = new Vec<Propagatable>();
			this.watches[(var << 1) | 1] = new Vec<Propagatable>();
			this.undos[var] = new Vec<Undoable>();
			this.level[var] = -1;
			this.falsified[var << 1] = false; // because truthValue[var] is
			// UNDEFINED
			this.falsified[var << 1 | 1] = false; // because truthValue[var] is
			// UNDEFINED
		}
		return lit;
	}

	@Override
	public boolean belongsToPool(int x) {
		assert x > 0;
		if (x >= this.pool.length) {
			return false;
		}
		return this.pool[x];
	}

	@Override
	public void resetPool() {
		for (int i = 0; i < this.pool.length; i++) {
			if (this.pool[i]) {
				reset(i << 1);
			}
		}
	}

	@Override
	public void ensurePool(int howmany) {
		if (howmany >= this.pool.length) {
			init(Math.max(howmany, this.pool.length << 1));
		}
		this.maxvarid = howmany;
	}

	@Override
	public void unassign(int lit) {
		assert this.falsified[lit] || this.falsified[lit ^ 1];
		this.falsified[lit] = false;
		this.falsified[lit ^ 1] = false;
	}

	@Override
	public void satisfies(int lit) {
		assert !this.falsified[lit] && !this.falsified[lit ^ 1];
		this.falsified[lit] = false;
		this.falsified[lit ^ 1] = true;
	}

	@Override
	public boolean isSatisfied(int lit) {
		return this.falsified[lit ^ 1];
	}

	@Override
	public final boolean isFalsified(int lit) {
		return this.falsified[lit];
	}

	@Override
	public boolean isUnassigned(int lit) {
		return !this.falsified[lit] && !this.falsified[lit ^ 1];
	}

	@Override
	public String valueToString(int lit) {
		if (isUnassigned(lit)) {
			return "?"; //$NON-NLS-1$
		}
		if (isSatisfied(lit)) {
			return "T"; //$NON-NLS-1$
		}
		return "F"; //$NON-NLS-1$
	}

	@Override
	public int nVars() {
		// return pool.length - 1;
		return this.maxvarid;
	}

	@Override
	public int not(int lit) {
		return lit ^ 1;
	}

	public static String toString(int lit) {
		return ((lit & 1) == 0 ? "" : "-") + (lit >> 1); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public void reset(int lit) {
		this.watches[lit].clear();
		this.watches[lit ^ 1].clear();
		this.level[lit >> 1] = -1;
		this.reason[lit >> 1] = null;
		this.undos[lit >> 1].clear();
		this.falsified[lit] = false;
		this.falsified[lit ^ 1] = false;
	}

	@Override
	public int getLevel(int lit) {
		return this.level[lit >> 1];
	}

	@Override
	public void setLevel(int lit, int l) {
		this.level[lit >> 1] = l;
	}

	@Override
	public IConstr getReason(int lit) {
		return this.reason[lit >> 1];
	}

	@Override
	public void setReason(int lit, IConstr r) {
		this.reason[lit >> 1] = r;
	}

	@Override
	public IVec<Undoable> undos(int lit) {
		return this.undos[lit >> 1];
	}

	@Override
	public void watch(int lit, Propagatable c) {
		this.watches[lit].push(c);
	}

	@Override
	public void removeWatch(int lit, Propagatable c) {
		this.watches[lit].remove(c);
	}

	@Override
	public IVec<Propagatable> watches(int lit) {
		return this.watches[lit];
	}

	@Override
	public boolean isImplied(int lit) {
		int var = lit >> 1;
		assert (this.reason[var] == null) || this.falsified[lit]
				|| this.falsified[lit ^ 1];
		// a literal is implied if it is a unit clause, ie
		// propagated without reason at decision level 0.
		return this.pool[var]
				&& ((this.reason[var] != null) || (this.level[var] == 0));
	}

	@Override
	public int realnVars() {
		return this.realnVars;
	}

	/**
	 * To get the capacity of the current vocabulary.
	 * 
	 * @return the total number of variables that can be managed by the
	 *         vocabulary.
	 */
	protected int capacity() {
		return this.pool.length - 1;
	}

	/**
	 * @since 2.1
	 */
	@Override
	public int nextFreeVarId(boolean reserve) {
		if (reserve) {
			// Adds a new maximum variable
			ensurePool(this.maxvarid + 1);
			// ensure pool changes maxvarid

			return this.maxvarid;
		}
		return this.maxvarid + 1;
	}

	/**
	 * Takes an internal IVecInt and inits its variables
	 * 
	 * @param in
	 */
	@Override
	public void initVariables(IVecInt in) {
		for (int i = 0; i < in.size(); i++) {
			assert (in.get(i) != 0);
			initVariable(in.get(i));
		}
	}

	
	private void initVariable(int lit) {
		// Separated method because this is currently a hack should be replaced
		// with better solution
		getFromPool(toDimacs(lit));
	}

	@Override
	public int getFreeVariable() {
		int d = this.nextFreeVarId(true);
		this.initVariable(toInternal(d));
		return d;
	}
	
}
