/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf.impl;

import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.Relation;

public class PackageVersionConstraintImpl implements PackageVersionConstraint {
	protected static final String PACKAGE_EDEFAULT = null;
	protected String package_ = PACKAGE_EDEFAULT;

	protected static final Relation REL_EDEFAULT = null;

	protected Relation rel = REL_EDEFAULT;

	protected static final int VERSION_EDEFAULT = -1;

	protected int version = VERSION_EDEFAULT;

	protected PackageVersionConstraintImpl() {
		super();
	}

	@Override
	public String getPackage() {
		return this.package_;
	}

	@Override
	public void setPackage(String newPackage) {
		this.package_ = newPackage;
	}

	@Override
	public Relation getRel() {
		return this.rel;
	}

	@Override
	public void setRel(Relation newRel) {
		this.rel = newRel == null ? REL_EDEFAULT : newRel;
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
	public String toString() {

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (package: ");
		result.append(this.package_);
		result.append(", rel: ");
		result.append(this.rel);
		result.append(", version: ");
		result.append(this.version);
		result.append(')');
		return result.toString();
	}

	@Override
	public int hashCode() {

		return (getPackage() + getRel() + this.version).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PackageVersionConstraint)) {
			return false;
		}
		PackageVersionConstraint other = (PackageVersionConstraint) obj;

		if (!(getPackage().equals(other.getPackage()))) {
			return false;
		}
		if (getRel() == null) {
			return true;
		} else {
			if (!(getRel().equals(other.getRel()))) {
				return false;
			}
			if (getVersion() != other.getVersion()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toCUDF() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.package_).append(" ");
		if (this.rel != null) {
			sb.append(this.rel.getLiteral()).append(" ");
			sb.append(this.version).append(" ");
		}
		return sb.toString();
	}

} // PackageVersionConstraintImpl
