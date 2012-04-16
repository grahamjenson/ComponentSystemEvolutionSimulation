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
package nz.geek.maori.sat4j.constraints.factory;

import static java.math.BigInteger.ONE;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.neg;

import java.math.BigInteger;

import nz.geek.maori.sat4j.constraints.BinaryClause;
import nz.geek.maori.sat4j.constraints.UnitClause;
import nz.geek.maori.sat4j.constraints.impl.HTClause;
import nz.geek.maori.sat4j.constraints.impl.MaxWatchPb;
import nz.geek.maori.sat4j.constraints.impl.WLClause;
import nz.geek.maori.sat4j.core.DataStructureFactory;
import nz.geek.maori.sat4j.core.Vec;
import nz.geek.maori.sat4j.core.VecInt;
import nz.geek.maori.sat4j.specs.ContradictionException;
import nz.geek.maori.sat4j.specs.IConstr;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.IVecInt;

public class ConstraintFactory implements PBDataStructureFactory,
		DataStructureFactory {

	private static ConstraintFactory INSTANCE;

	public static ConstraintFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ConstraintFactory();
		}
		return INSTANCE;

	}

	private ConstraintFactory() {

	}

	private INormalizer norm = INormalizer.FOR_COMPETITION;

	@Override
	public IConstr createConstraint(IVecInt literals) {
		// If Empty throw a fit
		if ((literals == null) || (literals.size() <= 0)) {
			return null;
		}

		// If the wanted constraint is a tautology then return null
		if (isTautology(literals)) {
			return null;
		}

		if (literals.size() == 1) {
			return new UnitClause(literals.last());
		}
		if (literals.size() == 2) {
			return new BinaryClause(literals);
		}
		return new HTClause(literals);
	}

	// *****************

	@Override
	public IConstr createPseudoBooleanConstraint(IVecInt literals,
			IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
			throws ContradictionException {
		// If a tautology return null (difficult to prove so ignored)
		// If impossible to satisfy throw Contradiction
		// TODO ^^

		// if it is less than, and there are no literals, then return null as it
		// is obviously satisfied
		if ((literals.size() == 0) && !moreThan) {
			return null;
		}

		PBContainer res = this.norm.nice(literals, coefs, moreThan, degree);

		BigInteger maxValue = BigInteger.valueOf(0);
		for (BigInteger bi : res.coefs) {
			maxValue = maxValue.add(bi);
		}
		if (maxValue.compareTo(res.degree) < 0) {
			throw new ContradictionException();
		}

		return constraintFactory(res.lits, res.coefs, res.degree);
	}

	static final BigInteger MAX_INT_VALUE = BigInteger
			.valueOf(Integer.MAX_VALUE);

	protected IConstr constraintFactory(int[] literals, BigInteger[] coefs,
			BigInteger degree) {
		// If the degree is one then it is HTClause representatble
		if (degree == null) {
			return null;
		}
		if (degree.equals(BigInteger.ONE)) {
			return createConstraint(new VecInt(literals));
		}

		// else
		return constructPB(literals, coefs, degree);
	}

	protected IConstr constructPB(int[] theLits, BigInteger[] coefs,
			BigInteger degree) {
		return new MaxWatchPb(theLits, coefs, degree);
	}

	// *****************

	public IConstr createCardinalityConstraint(VecInt cons, boolean morethan,
			int degree) throws ContradictionException {
		Vec<BigInteger> coefs = new Vec<BigInteger>();
		for (int i = 0; i < cons.size(); i++) {
			coefs.push(ONE);
		}
		return createPseudoBooleanConstraint(cons, coefs, morethan,
				BigInteger.valueOf(degree));
	}

	public IConstr createConflictSet(VecInt cons) throws ContradictionException {
		return createCardinalityConstraint(cons, false, 1);
	}

	/**
	 * Makes sure a constraint is not a tautology
	 * 
	 * @param literals
	 * @return
	 */
	private boolean isTautology(IVecInt literals) {
		// If it is unit or empty it is not a tautology
		if (literals.size() <= 1) {
			return false;
		}

		for (int i = 0; i < literals.size(); i++) {
			// If it contains the negative
			if (literals.contains(neg(literals.get(i)))) {
				return true;
			}
		}
		return false;
	}

}
