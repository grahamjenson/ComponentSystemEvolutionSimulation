/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf;

import java.util.List;
import java.util.Set;

public interface PackageList {

	Set<PackageVersionConstraint> getList();

	// Assumed to have the comma "," spearator
	String toCUDF();

	String toCUDF(char seperator);
} // PackageList
