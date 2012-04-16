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

import nz.geek.maori.sat4j.core.LearningStrategy;
import nz.geek.maori.sat4j.core.Solver;
import nz.geek.maori.sat4j.core.VarActivityListener;
import nz.geek.maori.sat4j.specs.IConstr;

/**
 * MiniSAT learning scheme.
 * 
 * The Data Structure Factory is expected to be set thanks to the appropriate
 * setter method before using it.
 * 
 * It was not possible to set it in the constructor.
 * 
 * @author leberre
 */
public final class MiniSATLearning implements LearningStrategy {
	private static final long serialVersionUID = 1L;

	protected VarActivityListener val;

	protected Solver s;

	@Override
	public void setVarActivityListener(VarActivityListener s) {
		this.val = s;
	}

	@Override
	public void setSolver(Solver s) {
		this.val = s;
		this.s = s;
	}

	public final void claBumpActivity(IConstr reason) {
		for (int i = 0; i < reason.size(); i++) {
			int q = reason.get(i);
			assert q > 1;
			this.val.varBumpActivity(q);
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void learns(IConstr constr) {
		// va contenir une nouvelle clause ou null si la clause est unitaire
		claBumpActivity(constr);
		this.s.learn(constr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Learn all clauses as in MiniSAT";
	}

}
