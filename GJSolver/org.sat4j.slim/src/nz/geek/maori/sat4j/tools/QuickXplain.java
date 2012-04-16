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
package nz.geek.maori.sat4j.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import nz.geek.maori.sat4j.core.Solver;
import nz.geek.maori.sat4j.core.SolverFactory;
import nz.geek.maori.sat4j.specs.IConstr;
import nz.geek.maori.sat4j.specs.ISolver;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.TimeoutException;

/**
 * An implementation of the QuickXplain algorithm as explained by Ulrich Junker
 * in the following paper:
 * 
 * @inproceedings{ junker01:quickxplain:inp, author={Ulrich Junker},
 *                 title={QUICKXPLAIN: Conflict Detection for Arbitrary
 *                 Constraint Propagation Algorithms}, booktitle={IJCAI'01
 *                 Workshop on Modelling and Solving problems with constraints
 *                 (CONS-1)}, year={2001}, month={August}, address={Seattle, WA,
 *                 USA}, url={citeseer.ist.psu.edu/junker01quickxplain.html},
 *                 url={http://www.lirmm.fr/~bessiere/ws_ijcai01/junker.ps.gz} }
 * 
 *                 The algorithm has been adapted to work properly in a context
 *                 where we can afford to add a selector variable to each clause
 *                 to enable or disable each constraint.
 * 
 *                 Note that for the moment, QuickXplain does not work properly
 *                 in an optimization setting.
 * 
 */
public class QuickXplain {

	/**
	 * The goal of Xplain algorithms is to return a minimal set of constraints
	 * that cause a conflict
	 * 
	 * @param c
	 * @param u
	 * @return
	 * @throws TimeoutException
	 */
	public static IConstr RobustExplain(Collection<IConstr> c,
			Collection<IConstr> u) throws TimeoutException {
		Solver s = SolverFactory.newDefault();
		s.addConstrs(c);
		new ArrayList<IConstr>();
		for (IConstr uc : u) {
			System.out.println(uc);
			s.addConstr(uc);
			if (!s.isSatisfiable()) {

				return uc;
			}
		}
		return null;
		// if(s.isSatisfiable()) return null;
		// IConstr ncon = s.counterConstr;
		//
		// ArrayList<IConstr> conflictSet = arrayList;
		// We know that the last element added is in the conflict set
		//
		// IConstr pop = added.pop();
		// conflictSet.add(pop);
		//
		// while(!added.isEmpty())
		// {
		// pop = added.pop();
		// s.removeConstr(pop);
		// if(s.isSatisfiable())
		// {
		// conflictSet.add(pop);
		// s.addConstr(pop);
		// }
		// else
		// {
		//
		// }
		// }
		//
		// return conflictSet;
	}

	private boolean computationCanceled;

	public void cancelExplanationComputation() {
		this.computationCanceled = true;
	}

	public HashSet<IConstr> explain(IVec<IConstr> cons) throws TimeoutException {
		IConstr[] U = new IConstr[cons.size()];
		for (int i = 0; i < cons.size(); i++) {
			U[i] = cons.get(i);
		}
		return explain(new HashSet<IConstr>(), U);
	}

	/**
	 * U is the set of all constraints C is the input set of constraints
	 * 
	 * @param solver
	 * @param cons
	 *            U
	 * @throws TimeoutException
	 */
	private HashSet<IConstr> explain(HashSet<IConstr> C, IConstr[] U)
			throws TimeoutException {
		ISolver solver = SolverFactory.newDefault();
		solver.addConstrs(C);

		if (!solver.isSatisfiable()) {
			return new HashSet<IConstr>();
		}
		if (U.length == 0) {
			throw new UnsupportedOperationException("No conflict");
		}

		IConstr[] a = U;
		int k = 0;
		int n = a.length;

		// Add a constraint at a time to find first conflict
		while (!this.computationCanceled && solver.isSatisfiable() && (k < n)) {
			IConstr c = a[k];
			k++;
			C.add(c);
			solver.addConstr(c);
		}

		// Added all constraints, found no conflict
		if (k == n - 1) {
			throw new UnsupportedOperationException("No conflict");
		}

		HashSet<IConstr> X = new HashSet<IConstr>();
		X.add(a[k]);

		// split(k-1)
		int i = (k - 1) / 2;

		IConstr[] U1 = Arrays.copyOfRange(a, 0, i);

		IConstr[] U2 = Arrays.copyOfRange(a, i, (k - 1));

		if (U1.length > 0) {
			HashSet<IConstr> D1 = explain(X, U1);
			X.addAll(D1);
		}

		if (U2.length > 0) {
			HashSet<IConstr> D2 = explain(X, U2);
			X.addAll(D2);

		}
		return X;
	}
}
