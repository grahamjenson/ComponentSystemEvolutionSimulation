/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf;


public interface PackageVersionConstraint {

	String getPackage();

	void setPackage(String value);

	Relation getRel();

	void setRel(Relation value);

	int getVersion();

	void setVersion(int value);

	String toCUDF();

} // PackageVersionConstraint
