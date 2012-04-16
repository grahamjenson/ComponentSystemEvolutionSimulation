/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface PackageFormula {
	Set<PackageList> getAnd();

	String toCUDF();

	public static class TRUE implements PackageFormula {

		@Override
		public Set<PackageList> getAnd() {
			return new HashSet<PackageList>();
		}

		@Override
		public String toCUDF() {
			return "true!";
		}

	}

	public static class FALSE implements PackageFormula {

		@Override
		public Set<PackageList> getAnd() {
			return new HashSet<PackageList>();
		}

		@Override
		public String toCUDF() {
			return "false!";
		}
	}
	
	
} // PackageFormula
