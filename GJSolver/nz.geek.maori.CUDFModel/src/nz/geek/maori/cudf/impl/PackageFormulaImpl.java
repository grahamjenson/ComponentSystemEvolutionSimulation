/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.geek.maori.cudf.PackageFormula;
import nz.geek.maori.cudf.PackageList;

public class PackageFormulaImpl implements PackageFormula {

	protected Set<PackageList> and;

	protected PackageFormulaImpl() {
		super();
	}

	@Override
	public Set<PackageList> getAnd() {
		if (this.and == null) {
			this.and = new HashSet<PackageList>();
		}
		return this.and;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + getAnd().hashCode();
		return hash;
	}

	@Override
	public String toCUDF() {
		StringBuffer sb = new StringBuffer();
		String delim = "";
		for (PackageList pl : this.and) {
			sb.append(delim).append(pl.toCUDF('|'));
			delim = ", ";
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PackageFormula))
		{
			return false;
		}
		PackageFormula pf = (PackageFormula)obj;

		return this.getAnd().equals(pf.getAnd());
	}

	@Override
	public String toString() {
		return toCUDF();
	}

}
