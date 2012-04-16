/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Keep;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageFormula;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.Preamble;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Relation;
import nz.geek.maori.cudf.Request;

public class ProfileChangeRequestImpl implements ProfileChangeRequest {
	protected Preamble preamble;

	// Universe
	protected ArrayList<nz.geek.maori.cudf.Package> installed = new ArrayList<nz.geek.maori.cudf.Package>(
			1000);
	protected ArrayList<nz.geek.maori.cudf.Package> ninstalled = new ArrayList<nz.geek.maori.cudf.Package>(
			10000);

	// Lookup Tables
	protected Map<String, Map<Integer, nz.geek.maori.cudf.Package>> namedInstalledPackages = new HashMap<String, Map<Integer, Package>>();
	protected Map<String, Map<Integer, nz.geek.maori.cudf.Package>> namedNInstalledPackages = new HashMap<String, Map<Integer, Package>>();

	// As two packages can provide exactly the same feature/version, we need
	// another set
	protected Map<String, Map<Integer, Collection<nz.geek.maori.cudf.Package>>> features = new HashMap<String, Map<Integer, Collection<nz.geek.maori.cudf.Package>>>(
			0);

	protected Request request;

	protected ProfileChangeRequestImpl() {
		super();
	}

	@Override
	public Preamble getPreamble() {
		return this.preamble;
	}

	@Override
	public void setPreamble(Preamble newPreamble) {
		this.preamble = newPreamble;
	}

	@Override
	public Request getRequest() {
		return this.request;
	}

	@Override
	public void setRequest(Request newRequest) {
		this.request = newRequest;
	}

	@Override
	public void addPackage(Package pack) {

		// if(installed.contains(pack) || ninstalled.contains(pack)) return;

		// Group into installed
		if (pack.isInstalled()) {
			this.installed.add(pack);
			Map<Integer, nz.geek.maori.cudf.Package> packs = this.namedInstalledPackages
			.get(pack.getName());
			if (packs == null) {
				packs = new HashMap<Integer, nz.geek.maori.cudf.Package>();
				this.namedInstalledPackages.put(pack.getName(), packs);
			}
			packs.put(pack.getVersion(), pack);
		} else {
			this.ninstalled.add(pack);
			Map<Integer, nz.geek.maori.cudf.Package> packs = this.namedNInstalledPackages
			.get(pack.getName());
			if (packs == null) {
				packs = new HashMap<Integer, nz.geek.maori.cudf.Package>();
				this.namedNInstalledPackages.put(pack.getName(), packs);
			}
			packs.put(pack.getVersion(), pack);
		}

		// Map the features, a large assumption is that feature and pacakage
		// name spaces dont conflict
		if (pack.getProvides() != null) {
			for (PackageVersionConstraint p : pack.getProvides().getList()) {
				// If the relationship does not equal null and the relationships
				// does not equal equals then)
				if ((p.getRel() != null) && (p.getRel() != Relation.EQUALS)) {
					throw new UnsupportedOperationException();
				}

				if (p.getRel() == null) {
					Map<Integer, Collection<Package>> versionMap = this.features
					.get(p.getPackage());
					if (versionMap == null) {
						versionMap = new HashMap<Integer, Collection<Package>>();
						this.features.put(p.getPackage(), versionMap);
					}

					// Version of -1 denotes that it is all versions
					Collection<Package> componentCol = versionMap.get(p
							.getVersion());
					if (componentCol == null) {
						componentCol = new ArrayList<Package>();
						versionMap.put(p.getVersion(), componentCol);
					}

					componentCol.add(pack);

				}

			}
		}
	}

	private HashMap<PackageVersionConstraint, Collection<Package>> cacheInstalled = new HashMap<PackageVersionConstraint, Collection<Package>>();
	private HashMap<PackageVersionConstraint, Collection<Package>> cacheNotInstalled = new HashMap<PackageVersionConstraint, Collection<Package>>();

	private boolean cache = false;

	@Override
	public Collection<Package> getPackagesThatSatisfy(
			PackageVersionConstraint constraint, boolean onlyInstalled) {

		if (this.cache) {

			if (onlyInstalled) {
				Collection<Package> cached = this.cacheInstalled
				.get(constraint);
				if (cached != null) {
					System.out.println("cached");
					return cached;
				}
			} else {
				Collection<Package> cached = this.cacheNotInstalled
				.get(constraint);
				if (cached != null) {
					System.out.println("cached");
					return cached;
				}
			}
		}

		HashSet<nz.geek.maori.cudf.Package> ret = new HashSet<nz.geek.maori.cudf.Package>();

		// Get the features that fulfill if any
		ret.addAll(getFeaturesThatSatisfy(constraint));
		Collection<nz.geek.maori.cudf.Package> possibles = null;

		if (onlyInstalled) {
			possibles = getInstalledPackageVersions(constraint.getPackage());
		} else {
			possibles = getPackageVersions(constraint.getPackage());
		}

		if (constraint.getRel() == null) {
			ret.addAll(possibles);
			return ret;
		} else if (constraint.getRel() == Relation.EQUALS) {
			Package package1 = getPackage(constraint.getPackage(),
					constraint.getVersion());
			if (package1 != null) {
				ret.add(package1);
			}
			// can only be one so can return now
			return ret;

		} else if (constraint.getRel() == Relation.L) {
			for (nz.geek.maori.cudf.Package pack : possibles) {
				if (pack.getVersion() < constraint.getVersion()) {
					ret.add(pack);
				}
			}
		} else if (constraint.getRel() == Relation.LEQ) {
			for (nz.geek.maori.cudf.Package pack : possibles) {
				if (pack.getVersion() <= constraint.getVersion()) {
					ret.add(pack);
				}
			}
		} else if (constraint.getRel() == Relation.G) {
			for (nz.geek.maori.cudf.Package pack : possibles) {
				if (pack.getVersion() > constraint.getVersion()) {
					ret.add(pack);
				}
			}
		} else if (constraint.getRel() == Relation.GEQ) {
			for (nz.geek.maori.cudf.Package pack : possibles) {
				if (pack.getVersion() >= constraint.getVersion()) {
					ret.add(pack);
				}
			}
		} else if (constraint.getRel() == Relation.NEQ) {
			for (nz.geek.maori.cudf.Package pack : possibles) {
				if (pack.getVersion() != constraint.getVersion()) {
					ret.add(pack);
				}
			}
		}

		if (this.cache) {
			if (onlyInstalled) {
				this.cacheInstalled.put(constraint, ret);

			} else {
				this.cacheNotInstalled.put(constraint, ret);

			}
		}

		return ret;
	}

	private Collection<Package> getFeaturesThatSatisfy(
			PackageVersionConstraint constraint) {
		HashSet<Package> ret = new HashSet<Package>();
		Map<Integer, Collection<Package>> versionMap = this.features
		.get(constraint.getPackage());
		if (versionMap == null) {
			return ret;
		}

		// Add all the versions that are any version as no matter the constraint
		// this will satisfy it
		ret.addAll(versionMap.get(-1));

		if (constraint.getRel() == null) {
			for (Integer i : versionMap.keySet()) {
				// If no relation is specified add all the possible components
				ret.addAll(versionMap.get(i));
			}
			return ret;
		} else if (constraint.getRel() == Relation.EQUALS) {

			ret.addAll(versionMap.get(constraint.getVersion()));
			return ret;
		} else if (constraint.getRel() == Relation.L) {
			for (Integer i : versionMap.keySet()) {
				if (i < constraint.getVersion()) {
					ret.addAll(versionMap.get(i));
				}
			}
		} else if (constraint.getRel() == Relation.LEQ) {
			for (Integer i : versionMap.keySet()) {
				if (i <= constraint.getVersion()) {
					ret.addAll(versionMap.get(i));
				}
			}
		} else if (constraint.getRel() == Relation.G) {
			for (Integer i : versionMap.keySet()) {
				if (i > constraint.getVersion()) {
					ret.addAll(versionMap.get(i));
				}
			}
		} else if (constraint.getRel() == Relation.GEQ) {
			for (Integer i : versionMap.keySet()) {
				if (i >= constraint.getVersion()) {
					ret.addAll(versionMap.get(i));
				}
			}
		} else if (constraint.getRel() == Relation.NEQ) {
			for (Integer i : versionMap.keySet()) {
				if (i != constraint.getVersion()) {
					ret.addAll(versionMap.get(i));
				}
			}
		}

		return ret;
	}

	@Override
	public String toCUDF() {
		StringBuffer sb = new StringBuffer();
		String EOL = System.getProperty("line.separator");
		if (this.preamble != null) {
			sb.append(this.preamble.toCUDF());
		}

		sb.append("# List of Installed Packages").append(EOL);

		for (Package p : this.installed) {
			sb.append(p.toCUDF());
		}

		sb.append("# List of Uninstalled Packages").append(EOL);
		for (Package p : this.ninstalled) {
			sb.append(p.toCUDF());
		}

		if (this.request != null) {
			sb.append(this.request.toCUDF());
		}
		sb.append(EOL);
		return sb.toString();
	}

	@Override
	public Package getPackage(String name, int version) {
		Map<Integer, Package> map = this.namedInstalledPackages.get(name);
		Package pack = null;
		if (map != null) {
			pack = map.get(version);
		}
		if (pack == null) {
			Map<Integer, Package> map2 = this.namedNInstalledPackages.get(name);
			if (map2 == null) {
				return null;
			}
			pack = map2.get(version);
		}
		return pack;
	}

	@Override
	public Collection<Package> getUniverse() {
		ArrayList<Package> packs = new ArrayList<Package>();
		packs.addAll(this.installed);
		packs.addAll(this.ninstalled);

		return packs;
	}

	@Override
	public Collection<Package> getInstalledPackages() {

		return this.installed;
	}

	@Override
	public Collection<Package> getNotInstalledPackages() {
		return this.ninstalled;
	}

	@Override
	public Collection<Package> getPackageVersions(String name) {
		return this.getPackageVersions(name, false);
	}

	@Override
	public Collection<Package> getPackageVersions(String name, boolean onlyinstalled) {
		ArrayList<Package> packs = new ArrayList<Package>();
		Map<Integer, Package> map = this.namedInstalledPackages.get(name);
		if (map != null) {
			packs.addAll(map.values());
		}
		if(!onlyinstalled)
		{
			Map<Integer, Package> map2 = this.namedNInstalledPackages.get(name);
			if (map2 != null) {
				packs.addAll(map2.values());
			}
		}
		return packs;
	}

	@Override
	public Collection<Package> getInstalledPackageVersions(String name) {
		Map<Integer, Package> map = this.namedInstalledPackages.get(name);
		if (map == null) {
			return new ArrayList<nz.geek.maori.cudf.Package>();
		}
		return map.values();
	}

	@Override
	public Collection<Package> getNotInstalledPackageVersions(String name) {
		Map<Integer, Package> map = this.namedNInstalledPackages.get(name);
		if (map == null) {
			return new ArrayList<nz.geek.maori.cudf.Package>();
		}
		return map.values();
	}

	@Override
	public Package getInstalledPackageVersion(String name, int version) {
		Map<Integer, Package> map = this.namedInstalledPackages.get(name);
		if (map == null) {
			return null;
		}
		return map.get(version);
	}

	@Override
	public Package getNInstalledPackageVersion(String name, int version) {
		Map<Integer, Package> map = this.namedNInstalledPackages.get(name);
		if (map == null) {
			return null;
		}
		return map.get(version);
	}

	@Override
	public Collection<Package> getPackagesThatSatisfy(PackageList constraint,
			boolean onlyInstalled) {
		HashSet<nz.geek.maori.cudf.Package> ret = new HashSet<nz.geek.maori.cudf.Package>();

		for (PackageVersionConstraint pvc : constraint.getList()) {
			ret.addAll(getPackagesThatSatisfy(pvc, onlyInstalled));
		}

		return ret;
	}

	@Override
	public String toString() {
		return toCUDF();
	}

	@Override
	public Collection<Package> getGreatestVersion(String name) {
		// Needs some fiddling on the part of virtual packages can have no
		// version, therefore should not be included.

		PackageVersionConstraint pvc = CUDFFactory.eINSTANCE.createPackageVersionConstraint();
		pvc.setPackage(name);
		int maxVersion = 0;

		Collection<Package> packagesThatSatisfy = this.getPackagesThatSatisfy(pvc);

		List<nz.geek.maori.cudf.Package> ret = new ArrayList<nz.geek.maori.cudf.Package>();

		// List to definitily return, virtual packages that supply all versions.
		List<nz.geek.maori.cudf.Package> defret = new ArrayList<nz.geek.maori.cudf.Package>();
		for (Package p : packagesThatSatisfy) {
			if (!p.getName().equals(name)) {
				// The package is virtual
				for (PackageVersionConstraint prov : p.getProvides().getList()) {
					if (prov.getPackage().equals(name)) {
						if (prov.getVersion() == -1) {
							defret.add(p);
						} else if (prov.getVersion() == maxVersion) {
							ret.add(p);
						} else if (prov.getVersion() > maxVersion) {
							ret = new ArrayList<nz.geek.maori.cudf.Package>();
							ret.add(p);
							maxVersion = prov.getVersion();
						}
					}
				}
			} else {
				if (p.getVersion() > maxVersion) {
					ret = new ArrayList<nz.geek.maori.cudf.Package>();
					ret.add(p);
					maxVersion = p.getVersion();
				}
			}
		}
		ret.addAll(defret);
		return ret;
	}

	@Override
	public Collection<Package> getPackagesThatSatisfy(PackageList constraint) {
		if (constraint == null) {
			throw new NullPointerException();
		}
		return this.getPackagesThatSatisfy(constraint, false);
	}

	@Override
	public Collection<Package> getPackagesThatSatisfy(
			PackageVersionConstraint constraint) {
		return this.getPackagesThatSatisfy(constraint, false);
	}

	@Override
	public ProfileChangeRequest slice() {
		if (getRequest() == null) {
			return this;
		}
		ProfileChangeRequest newPCR = CUDFFactory.eINSTANCE
		.createProfileChangeRequest();
		newPCR.setPreamble(this.preamble);
		newPCR.setRequest(this.request);

		Stack<Package> toVisit = new Stack<Package>();
		HashSet<Package> visited = new HashSet<Package>();

		for (Package p : getInstalledPackages()) {
			toVisit.addAll(getPackageVersions(p.getName()));
			if (p.getKeep() != Keep.NONE) {
				if (p.getKeep() == Keep.VERSION) {
					// A unit constraint that the package must remain in the
					// solution

				} else if (p.getKeep() == Keep.PACKAGE) {
					// Constraint such that at least one component must be in
					// the solution
					Collection<Package> comps = getPackageVersions(p.getName());
					for (Package c : comps) {
						toVisit.push(c);
					}

				} else if (p.getKeep() == Keep.FEATURE) {
					for (PackageVersionConstraint pvc : p.getProvides()
							.getList()) {
						Collection<Package> packagesThatSatisfy = this
						.getPackagesThatSatisfy(pvc);

						for (Package c : packagesThatSatisfy) {
							toVisit.push(c);
						}

					}
				}
			}
		}
		// Any package that can
		if (getRequest().getInstall() != null) {
			for (Package p : this.getPackagesThatSatisfy(getRequest()
					.getInstall())) {
				toVisit.add(p);
			}
		}

		if (getRequest().getUpgrade() != null) {
			for (Package p : this.getPackagesThatSatisfy(getRequest()
					.getUpgrade())) {
				toVisit.add(p);
			}
		}

		while (toVisit.size() > 0) {
			Package p = toVisit.pop();
			if (visited.contains(p)) {
				continue;
			}
			visited.add(p);
			newPCR.addPackage(p);
			PackageFormula depends = p.getDepends();
			if (depends != null) {
				for (PackageList pl : depends.getAnd()) {
					for (Package dp : this.getPackagesThatSatisfy(pl)) {
						for(Package ver : this.getPackageVersions(dp.getName()))
						{
							if(dp.getVersion() <= ver.getVersion())
							{
								toVisit.push(ver);
							}
						}
					}
				}
			}
			PackageFormula recommends = (PackageFormula)p.getProperties().get("recommends");
			if (recommends != null) {
				for (PackageList pl : recommends.getAnd()) {
					for (Package dp : this.getPackagesThatSatisfy(pl)) {
						for(Package ver : this.getPackageVersions(dp.getName()))
						{
							if(dp.getVersion() <= ver.getVersion())
							{
								toVisit.push(ver);
							}
						}
					}
				}
			}
		}
		return newPCR;
	}

	public boolean isSameInstallation(ProfileChangeRequest pcr)
	{
		HashSet<String> innames = new HashSet<String>(pcr.getPackageNames(true));
		HashSet<String> mynames = new HashSet<String>(this.getPackageNames(true));
		
		if(!innames.equals(mynames))
		{
			return false;
		}
		
		for(String name : innames)
		{
			HashSet<Package> inpacks = new HashSet<Package>(pcr.getPackageVersions(name, true));
			HashSet<Package> mypacks = new HashSet<Package>(pcr.getPackageVersions(name, true));
			if(!(inpacks.equals(mypacks)))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Collection<String> getPackageNames(boolean onlyInstalled) {
		HashSet<String> names = new HashSet<String>();
		names.addAll(this.namedInstalledPackages.keySet());
		if (!onlyInstalled) {
			names.addAll(this.namedNInstalledPackages.keySet());
		}
		return names;
	}
}
