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
 *******************************************************************************/

package nz.geek.maori.sat4j.tools;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.neg;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.toInternal;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nz.geek.maori.sat4j.constraints.factory.ConstraintFactory;
import nz.geek.maori.sat4j.core.Vec;
import nz.geek.maori.sat4j.core.VecInt;
import nz.geek.maori.sat4j.specs.ContradictionException;
import nz.geek.maori.sat4j.specs.IConstr;
import nz.geek.maori.sat4j.specs.ISolver;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.TimeoutException;

/**
 * Helper class intended to make life easier to people to feed a sat solver
 * programmatically.
 * 
 * @author daniel
 * 
 * @param <T>
 *            The class of the objects to map into boolean variables.
 * @param <C>
 *            The class of the object to map to each constraint.
 */
public class DependencyHelper<T, C> {

	// Dimacs values
	private final Map<T, Integer> mapToDimacs = new HashMap<T, Integer>();
	private final Map<Integer, T> mapToDomain = new HashMap<Integer, T>();

	final Map<IConstr, C> descs = new HashMap<IConstr, C>();

	protected ISolver solver;
	private QuickXplain qe;
	private ConstraintFactory dsf = ConstraintFactory.getInstance();

	/**
	 * 
	 * @param solver
	 *            the solver to be used to solve the problem.
	 */
	public DependencyHelper(ISolver solver) {
		this.solver = solver;
	}

	/**
	 * Translate a domain object into a dimacs variable.
	 * 
	 * @param thing
	 *            a domain object
	 * @return the dimacs variable (an integer) representing that domain object.
	 */
	int getIntValue(T thing) {
		Integer ref = this.mapToDimacs.get(thing);
		if (ref == null) {
			ref = this.solver.getVocabulary().nextFreeVarId(true);
			this.mapToDimacs.put(thing, ref);
			this.mapToDomain.put(ref, thing);

		}
		return ref;
	}

	/**
	 * Retrieve the solution found.
	 * 
	 * @return the domain object that must be satisfied to satisfy the
	 *         constraints entered in the solver.
	 * @see {@link #hasASolution()}
	 */
	public IVec<T> getSolution(int model[]) {
		Vec<T> sol = new Vec<T>();
		for (int i : model) {
			// If it is mapped to a thing then negate it
			if (this.mapToDomain.containsKey(i)) {
				sol.push(this.mapToDomain.get(i));
			}
		}
		return sol;

		/*
		 * C = getConstraints(Problem) while(isSatisfiable(C)) { model =
		 * getAnswer() C = C U generateConstraints(model) } return model
		 */

	}

	// Given a Set of Constraints
	public Set<C> why(IVec<IConstr> constraints) throws TimeoutException {
		this.qe = new QuickXplain();
		HashSet<IConstr> explain = this.qe.explain(constraints);
		HashSet<C> c = new HashSet<C>();
		for (IConstr con : explain) {
			c.add(this.descs.get(con));
		}
		return c;
	}

	/**
	 * model is internal
	 * 
	 * @param model
	 * @return
	 */
	public IConstr notSameSolution(int[] model) {
		VecInt cons = new VecInt();
		for (int i : model) {
			// If it is mapped to a thing then negate it
			if (this.mapToDomain.containsKey(i)) {
				cons.push(neg(i));
			}
		}
		IConstr constr = this.dsf.createConstraint(cons);
		return constr;
	}

	// Constraint ensuring thing is in the solution
	public IConstr setTrue(T thing, C name) {

		IConstr constr = this.dsf.createConstraint(new VecInt(
				new int[] { toInternal(getIntValue(thing)) }));
		this.descs.put(constr, name);
		return constr;
	}

	// Constraint ensuring if lhs is in the solution something from rhs is in
	// the solution
	public IConstr dependsOn(C name, T lhs, IVec<T> rhs) {
		VecInt cons = new VecInt();
		cons.push(neg(toInternal(getIntValue(lhs))));
		for (int i = 0; i < rhs.size(); i++) {
			cons.push(toInternal(getIntValue(rhs.get(i))));
		}
		IConstr constr = this.dsf.createConstraint(cons);
		this.descs.put(constr, name);
		return constr;
	}

	// Set of conflicts
	public IConstr conflictSet(C name, IVec<T> things)
			throws ContradictionException {
		VecInt cons = new VecInt();
		Vec<BigInteger> coeffs = new Vec<BigInteger>();

		for (int i = 0; i < things.size(); i++) {
			cons.push(toInternal(getIntValue(things.get(i))));
			coeffs.push(BigInteger.ONE);
		}

		IConstr constr = this.dsf.createPseudoBooleanConstraint(cons, coeffs,
				false, BigInteger.ONE);
		this.descs.put(constr, name);
		return constr;
	}

	/**
	 * Stop the SAT solver that is looking for a solution. The solver will throw
	 * a TimeoutException.
	 */
	public void stopSolver() {
		this.solver.expireTimeout();
	}

	/**
	 * Stop the explanation computation. A TimeoutException will be thrown by
	 * the explanation algorithm.
	 */
	public void stopExplanation() {
		this.qe.cancelExplanationComputation();
	}

	public ISolver getSolver() {
		return this.solver;
	}
}
