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

import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.PackageVersionConstraint;

public class PackageListImpl implements PackageList {
	protected Set<PackageVersionConstraint> list = new HashSet<PackageVersionConstraint>();

	protected PackageListImpl() {
		super();
	}

	@Override
	public Set<PackageVersionConstraint> getList() {
		if (this.list == null) {
			this.list = new HashSet<PackageVersionConstraint>();
		}
		return this.list;
	}

	@Override
	public String toCUDF() {
		return toCUDF(',');
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PackageList))
		{
			return false;
		}
		PackageList pf = (PackageList)obj;
		
		return this.getList().equals(pf.getList());
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + this.list.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return toCUDF();
	}

	@Override
	public String toCUDF(char seperator) {
		StringBuffer sb = new StringBuffer();
		String delim = "";
		for (PackageVersionConstraint pl : this.list) {
			sb.append(delim).append(pl.toCUDF());
			delim = seperator + " ";
		}
		return sb.toString();
	}

} // PackageListImpl
