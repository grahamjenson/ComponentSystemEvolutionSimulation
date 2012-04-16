/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf.impl;

import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.Request;

public class RequestImpl implements Request {
	protected static final String NAME_EDEFAULT = null;

	protected String name = NAME_EDEFAULT;

	protected PackageList install;

	protected PackageList remove;

	protected PackageList upgrade;

	protected RequestImpl() {
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
	public PackageList getInstall() {
		return this.install;
	}

	@Override
	public void setInstall(PackageList newInstall) {
		this.install = newInstall;
	}

	@Override
	public PackageList getRemove() {
		return this.remove;
	}

	@Override
	public void setRemove(PackageList newRemove) {
		this.remove = newRemove;
	}

	@Override
	public PackageList getUpgrade() {
		return this.upgrade;
	}

	@Override
	public void setUpgrade(PackageList newUpgrade) {
		this.upgrade = newUpgrade;
	}

	@Override
	public String toString() {

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(this.name);
		result.append(')');
		return result.toString();
	}

	@Override
	public String toCUDF() {
		StringBuffer sb = new StringBuffer();
		String EOL = System.getProperty("line.separator");
		sb.append("request: ").append(EOL);
		if (this.install != null) {
			sb.append("install: ").append(this.install.toCUDF()).append(EOL);
		}

		if (this.remove != null) {
			sb.append("remove: ").append(this.remove.toCUDF()).append(EOL);
		}

		if (this.upgrade != null) {
			sb.append("upgrade: ").append(this.upgrade.toCUDF()).append(EOL);
		}

		return sb.toString();
	}

} // RequestImpl
