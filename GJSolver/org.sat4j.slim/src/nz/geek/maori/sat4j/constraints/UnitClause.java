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

import nz.geek.maori.sat4j.core.UnitPropagationListener;
import nz.geek.maori.sat4j.specs.ILits;
import nz.geek.maori.sat4j.specs.IVecInt;
import nz.geek.maori.sat4j.tools.LiteralsUtils;

/**
 * 
 * @author daniel
 * @since 2.1
 */
public class UnitClause implements Clause {

	protected final int lit;

	public UnitClause(int value) {
		this.lit = value;
	}

	@Override
	public void assertConstraint(UnitPropagationListener s) {
		s.enqueue(this.lit, this);
	}

	@Override
	public void calcReason(int p, IVecInt outReason) {
		outReason.push(LiteralsUtils.neg(this.lit));

	}

	@Override
	public double getActivity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void incActivity(double claInc) {
		// silent to prevent problems with xplain trick.
	}

	@Override
	public boolean locked() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void register() {

	}

	@Override
	public void remove(UnitPropagationListener upl) {
		// upl.unset(lit);
	}

	@Override
	public void rescaleBy(double d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLearnt() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean simplify() {
		return false;
	}

	@Override
	public boolean propagate(UnitPropagationListener s, int p) {
		return false;
	}

	@Override
	public int get(int i) {
		if (i > 0) {
			throw new IllegalArgumentException();
		}
		return this.lit;
	}

	@Override
	public boolean learnt() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void setVocabulary(ILits voc) {
		throw new UnsupportedOperationException(
				"Unit clauses do not need vocabulary");
	}

	@Override
	public ILits getVocabulary() {
		throw new UnsupportedOperationException(
				"Unit clauses do not need vocabulary");
	}

	@Override
	public int[] getLits() {
		return new int[] { this.lit };
	}

	@Override
	public String toString() {

		return "[" + LiteralsUtils.toDimacs(this.lit) + "= T ]";

	}
}
