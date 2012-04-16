package nz.geek.maori.cudf;

import java.util.Collection;

public interface ProfileChangeRequest {
	Preamble getPreamble();

	void setPreamble(Preamble value);

	void addPackage(nz.geek.maori.cudf.Package pack);

	void setRequest(Request value);

	Request getRequest();

	String toCUDF();

	ProfileChangeRequest slice();

	// Queries over the set of packages

	Package getPackage(String name, int version);

	Package getInstalledPackageVersion(String name, int version);

	Package getNInstalledPackageVersion(String name, int version);

	/**
	 * The reason this method has a collectino and not a single package, is that
	 * virtual packages of equal versions could be implemented by different
	 * packages, so we will return them all
	 * 
	 * @param name
	 * @return
	 */
	Collection<Package> getGreatestVersion(String name);

	Collection<String> getPackageNames(boolean onlyInstalled);

	Collection<nz.geek.maori.cudf.Package> getUniverse();

	Collection<nz.geek.maori.cudf.Package> getInstalledPackages();

	Collection<nz.geek.maori.cudf.Package> getNotInstalledPackages();

	Collection<nz.geek.maori.cudf.Package> getPackageVersions(String name);

	Collection<nz.geek.maori.cudf.Package> getPackageVersions(String name, boolean onlyInstalled);
	
	Collection<nz.geek.maori.cudf.Package> getInstalledPackageVersions(String name);

	Collection<nz.geek.maori.cudf.Package> getNotInstalledPackageVersions(String name);

	Collection<nz.geek.maori.cudf.Package> getPackagesThatSatisfy(PackageList constraint);

	Collection<nz.geek.maori.cudf.Package> getPackagesThatSatisfy(PackageList constraint, boolean onlyInstalled);

	Collection<nz.geek.maori.cudf.Package> getPackagesThatSatisfy(PackageVersionConstraint constraint);

	Collection<nz.geek.maori.cudf.Package> getPackagesThatSatisfy(PackageVersionConstraint constraint, boolean onlyInstalled);

	public boolean isSameInstallation(ProfileChangeRequest pcr);
	
} // ProfileChangeRequest
