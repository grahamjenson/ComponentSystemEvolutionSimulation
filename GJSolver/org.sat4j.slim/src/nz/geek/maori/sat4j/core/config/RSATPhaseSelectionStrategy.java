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

import static nz.geek.maori.sat4j.tools.LiteralsUtils.negLit;
import nz.geek.maori.sat4j.specs.IPhaseSelectionStrategy;

/**
 * Keeps track of the phase of the latest assignment.
 * 
 * @author leberre
 * 
 */
public final class RSATPhaseSelectionStrategy implements
		IPhaseSelectionStrategy {

	protected int[] phase;

	@Override
	public void init(int nlength) {
		this.phase = new int[nlength];
		for (int i = 1; i < nlength; i++) {
			this.phase[i] = negLit(i);
		}
	}

	@Override
	public void init(int var, int p) {
		this.phase[var] = p;
	}

	@Override
	public int select(int var) {
		return this.phase[var];
	}

	@Override
	public void assignLiteral(int p) {
		this.phase[p >> 1] = p;
	}

	@Override
	public String toString() {
		return "lightweight component caching from RSAT";
	}

	@Override
	public void updateVar(int p) {
	}
}
