/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf;

import java.util.Map;

public interface Package {
	String getName();

	void setName(String value);

	int getVersion();

	void setVersion(int value);

	PackageFormula getDepends();

	void setDepends(PackageFormula value);

	PackageList getConflicts();

	void setConflicts(PackageList value);

	PackageList getProvides();

	void setProvides(PackageList value);

	boolean isInstalled();

	void setInstalled(boolean value);

	boolean isWasInstalled();

	void setWasInstalled(boolean value);

	Keep getKeep();

	void setKeep(Keep value);

	Map<String, Object> getProperties();

	String toCUDF();

	public nz.geek.maori.cudf.Package clone();

} // Package
