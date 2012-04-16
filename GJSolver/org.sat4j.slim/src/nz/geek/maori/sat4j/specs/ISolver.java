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
package nz.geek.maori.sat4j.specs;

import java.util.Collection;

import nz.geek.maori.sat4j.core.SolverStats;

/**
 * This interface contains all services provided by a SAT solver.
 * 
 * @author leberre
 */
public interface ISolver {

	public ILits getVocabulary();

	/**
	 * Provide a model (if any) for a satisfiable formula. That method should be
	 * called AFTER isSatisfiable() or isSatisfiable(IVecInt) if the formula is
	 * satisfiable. Else an exception UnsupportedOperationException is launched.
	 * 
	 * @return a model of the formula as an array of literals to satisfy.
	 * @see #isSatisfiable()
	 * @see #isSatisfiable(IVecInt)
	 */
	int[] model();

	/**
	 * Check the satisfiability of the set of constraints contained inside the
	 * solver.
	 * 
	 * @return true if the set of constraints is satisfiable, else false.
	 */
	boolean isSatisfiable() throws TimeoutException;

	public IVec<IConstr> getConstraints();

	/**
	 * To know the number of constraints currently available in the solver.
	 * (without taking into account learned constraints).
	 * 
	 * @return the number of constraints added to the solver
	 */
	int nConstraints();

	/**
	 * To know the number of variables used in the solver.
	 * 
	 * @return the number of variables created using newVar().
	 */
	int nVars();

	public void addConstrs(Collection<IConstr> cons);

	public void addConstr(IConstr constr);

	/**
	 * Removes a constraint from the problem, If this is called and it changes
	 * the problem, call clearLearntClauses
	 * 
	 * @param c
	 * @return
	 */
	boolean removeConstr(IConstr c);

	/**
	 * To set the internal timeout of the solver. When the timeout is reached, a
	 * timeout exception is launched by the solver.
	 * 
	 * Here the timeout is given in number of conflicts. That way, the behavior
	 * of the solver should be the same across different architecture.
	 * 
	 * @param count
	 *            the timeout (in number of counflicts)
	 */
	void setConflictsStopCount(int count);

	/**
	 * Expire the timeout of the solver.
	 */
	void expireTimeout();

	/**
	 * Clean up the internal state of the solver.
	 */
	void reset();

	SolverStats getStats();

	/**
	 * Remove clauses learned during the solving process.
	 */
	void clearLearntClauses();

	/**
	 * Set whether the solver is allowed to simplify the formula by propagating
	 * the truth value of top level satisfied variables.
	 * 
	 * Note that the solver should not be allowed to perform such simplification
	 * when constraint removal is planned.
	 */
	void setDBSimplificationAllowed(boolean status);

	/**
	 * Indicate whether the solver is allowed to simplify the formula by
	 * propagating the truth value of top level satisfied variables.
	 * 
	 * Note that the solver should not be allowed to perform such simplification
	 * when constraint removal is planned.
	 */
	boolean isDBSimplificationAllowed();

	/**
	 * Allow the user to hook a listener to the solver to be notified of the
	 * main steps of the search process.
	 * 
	 * @param sl
	 *            a Search Listener.
	 * @since 2.1
	 */
	void setSearchListener(SearchListener sl);

}
