/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf.impl;

import java.util.HashMap;
import java.util.Map;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Keep;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageFormula;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.Type;

public class PackageImpl implements nz.geek.maori.cudf.Package {
	protected static final String NAME_EDEFAULT = null;

	protected String name = NAME_EDEFAULT;

	protected static final int VERSION_EDEFAULT = -1;

	protected int version = VERSION_EDEFAULT;

	protected PackageFormula depends;

	protected PackageList conflicts;

	protected PackageList provides;

	protected static final boolean INSTALLED_EDEFAULT = false;

	protected boolean installed = INSTALLED_EDEFAULT;

	protected static final boolean WAS_INSTALLED_EDEFAULT = false;

	protected boolean wasInstalled = WAS_INSTALLED_EDEFAULT;

	protected static final Keep KEEP_EDEFAULT = Keep.NONE;

	protected Keep keep = KEEP_EDEFAULT;

	protected Map<String, Object> properties;

	protected PackageImpl() {
		super();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public void setVersion(int newVersion) {
		this.version = newVersion;
	}

	@Override
	public PackageFormula getDepends() {
		return this.depends;
	}

	@Override
	public void setDepends(PackageFormula newDepends) {
		this.depends = newDepends;
	}

	@Override
	public PackageList getConflicts() {
		return this.conflicts;
	}

	@Override
	public void setConflicts(PackageList newConflicts) {
		this.conflicts = newConflicts;
	}

	@Override
	public void setProvides(PackageList newProvides) {
		this.provides = newProvides;
	}

	@Override
	public PackageList getProvides() {
		return this.provides;
	}

	@Override
	public boolean isInstalled() {
		return this.installed;
	}

	@Override
	public void setInstalled(boolean newInstalled) {
		this.installed = newInstalled;
	}

	@Override
	public boolean isWasInstalled() {
		return this.wasInstalled;
	}

	@Override
	public void setWasInstalled(boolean newWasInstalled) {
		this.wasInstalled = newWasInstalled;
	}

	@Override
	public Keep getKeep() {
		return this.keep;
	}

	@Override
	public void setKeep(Keep newKeep) {
		this.keep = newKeep == null ? KEEP_EDEFAULT : newKeep;
	}

	@Override
	public Map<String, Object> getProperties() {
		if (this.properties == null) {
			this.properties = new HashMap<String, Object>();
		}
		return this.properties;
	}

	@Override
	public String toString() {

		StringBuffer result = new StringBuffer();
		result.append(this.name);
		result.append("v");
		result.append(this.version);
		return result.toString();
	}

	@Override
	public String toCUDF() {
		StringBuffer sb = new StringBuffer();
		String EOL = System.getProperty("line.separator");
		sb.append("package: ").append(this.name).append(EOL);
		sb.append("version: ").append(this.version).append(EOL);
		if (this.depends != null) {
			sb.append("depends: ").append(this.depends.toCUDF()).append(EOL);
		}

		if (this.conflicts != null) {
			sb.append("conflicts: ").append(this.conflicts.toCUDF())
					.append(EOL);
		}

		if (this.provides != null) {
			sb.append("provides: ").append(this.provides.toCUDF()).append(EOL);
		}

		if (this.installed != INSTALLED_EDEFAULT) {
			sb.append("installed: ").append(this.installed).append(EOL);
		}

		if (this.wasInstalled != WAS_INSTALLED_EDEFAULT) {
			sb.append("was-installed: ").append(this.wasInstalled).append(EOL);
		}

		if (this.keep != KEEP_EDEFAULT) {
			sb.append("keep: ").append(this.keep.getLiteral()).append(EOL);
		}

		for (String p : getProperties().keySet()) {
			Object prop = getProperties().get(p);
			sb.append(p).append(": ");
			sb.append(Type.toCUDF(prop));
			sb.append(EOL);
		}
		sb.append(EOL);
		return sb.toString();
	}

	@Override
	public nz.geek.maori.cudf.Package clone() {
		Package cudfpackage = CUDFFactory.eINSTANCE.createPackage();
		cudfpackage.setName(getName());
		cudfpackage.setVersion(getVersion());
		return cudfpackage;
	}

	@Override
	public int hashCode() {
		int hash = 31 * getName().hashCode() + (getVersion() + 1);
		return hash;

	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Package)) {
			return false;
		}
		Package p1 = (Package) obj;
		return getName().equals(p1.getName())
				&& (getVersion() == p1.getVersion());
	}

} // PackageImpl
