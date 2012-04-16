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
package nz.geek.maori.sat4j.tools;

import java.math.BigInteger;

import nz.geek.maori.sat4j.core.Vec;
import nz.geek.maori.sat4j.specs.IVec;
import nz.geek.maori.sat4j.specs.IVecInt;

public abstract class PseudoUtils {

	// public static IVec<IConstr> OR(int lit, IConstr c1, IConstr c2) throws
	// ContradictionException
	// {
	// Vec<IConstr> vm = new Vec<IConstr>();
	// if(c1 instanceof Clause && c2 instanceof Clause)
	// {
	// //if they are both clauses then the constraint is simple
	// ConstraintFactory dsf = ConstraintFactory.getInstance();
	// VecInt c = new VecInt(c1.getLits());
	// c.pushAll(new VecInt(c2.getLits()));
	// vm.push(dsf.createConstraint(c));
	// return vm;
	// }
	//
	// if(c1 instanceof PBConstr)
	// {
	// vm.push(OR(lit,(PBConstr)c1));
	// }
	// else if(c1 instanceof Clause)
	// {
	// vm.push(OR(lit,(Clause)c1));
	// }
	// else
	// {
	// throw new UnsupportedOperationException();
	// }
	//
	// if(c2 instanceof PBConstr)
	// {
	// vm.push(OR(neg(lit),(PBConstr)c2));
	// }
	// else if(c2 instanceof Clause)
	// {
	// vm.push(OR(neg(lit),(Clause)c2));
	// }
	// else
	// {
	// throw new UnsupportedOperationException();
	// }
	//
	// return vm;
	// }

	public static BigInteger niceCheckedParametersForCompetition(int[] lits,
			BigInteger[] bc, boolean moreThan, BigInteger bigDeg) {
		BigInteger bigDegree = bigDeg;
		if (!moreThan) {
			for (int i = 0; i < lits.length; i++) {
				bc[i] = bc[i].negate();
			}
			bigDegree = bigDegree.negate();
		}

		for (int i = 0; i < bc.length; i++) {
			if (bc[i].signum() < 0) {
				lits[i] = lits[i] ^ 1;
				bc[i] = bc[i].negate();
				bigDegree = bigDegree.add(bc[i]);
			}
		}

		for (int i = 0; i < bc.length; i++) {
			if (bc[i].compareTo(bigDegree) > 0) {
				bc[i] = bigDegree;
			}
		}

		return bigDegree;

	}

	public static BigInteger niceParametersForCompetition(int[] ps,
			BigInteger[] bigCoefs, boolean moreThan, BigInteger bigDeg) {
		// Ajouter les simplifications quand la structure sera d?finitive
		if (ps.length == 0) {
			return null;
		} else if (ps.length != bigCoefs.length) {
			throw new IllegalArgumentException(
					"Contradiction dans la taille des tableaux ps=" + ps.length
							+ " coefs=" + bigCoefs.length + ".");
		}
		return niceCheckedParametersForCompetition(ps, bigCoefs, moreThan,
				bigDeg);
	}

	public static IVec<BigInteger> toVecBigInt(IVecInt vec) {
		IVec<BigInteger> bigVec = new Vec<BigInteger>(vec.size());
		for (int i = 0; i < vec.size(); ++i) {
			bigVec.push(BigInteger.valueOf(vec.get(i)));
		}
		return bigVec;
	}

	public static BigInteger toBigInt(int i) {
		return BigInteger.valueOf(i);
	}

}
