package nz.geek.maori.cudf.impl;

import java.util.HashMap;
import java.util.Map;

import nz.geek.maori.cudf.Preamble;
import nz.geek.maori.cudf.Type;

public class PreambleImpl implements Preamble {
	protected static final String UCHECKSUM_EDEFAULT = null;

	protected String uchecksum = UCHECKSUM_EDEFAULT;

	protected static final String SCHECKSUM_EDEFAULT = null;

	protected String schecksum = SCHECKSUM_EDEFAULT;

	protected static final String RCHECKSUM_EDEFAULT = null;

	protected String rchecksum = RCHECKSUM_EDEFAULT;

	protected Map<String, Type> types;

	protected Map<String, Object> defaults;

	protected PreambleImpl() {
		super();
	}

	@Override
	public String getUchecksum() {
		return this.uchecksum;
	}

	@Override
	public void setUchecksum(String newUchecksum) {
		this.uchecksum = newUchecksum;
	}

	@Override
	public String getSchecksum() {
		return this.schecksum;
	}

	@Override
	public void setSchecksum(String newSchecksum) {
		this.schecksum = newSchecksum;
	}

	@Override
	public String getRchecksum() {
		return this.rchecksum;
	}

	@Override
	public void setRchecksum(String newRchecksum) {
		this.rchecksum = newRchecksum;
	}

	@Override
	public Map<String, Type> getTypes() {
		if (this.types == null) {
			this.types = new HashMap<String, Type>();
		}
		return this.types;
	}

	@Override
	public Map<String, Object> getDefaults() {
		if (this.defaults == null) {
			this.defaults = new HashMap<String, Object>();
		}
		return this.defaults;
	}

	@Override
	public String toString() {

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (uchecksum: ");
		result.append(this.uchecksum);
		result.append(", schecksum: ");
		result.append(this.schecksum);
		result.append(", rchecksum: ");
		result.append(this.rchecksum);
		result.append(')');
		return result.toString();
	}

	@Override
	public String toCUDF() {
		StringBuffer sb = new StringBuffer();
		String EOL = System.getProperty("line.separator");
		sb.append("preamble: ").append(EOL);
		sb.append("property: ");
		String delim = "";
		for (String prop : this.types.keySet()) {
			sb.append(delim).append(prop).append(": ");
			delim = ", ";
			sb.append(this.types.get(prop).getLiteral());
			Object object = this.defaults.get(prop);
			if (object != null) {
				sb.append(" = [");
				sb.append(Type.toCUDF(object));
				sb.append("]");
			}
		}
		sb.append(EOL);
		if (this.uchecksum != null) {
			sb.append("univ-checksum: ").append(this.uchecksum).append(EOL);
		}

		if (this.schecksum != null) {
			sb.append("status-checksum: ").append(this.schecksum).append(EOL);
		}

		if (this.rchecksum != null) {
			sb.append("req-checksum: ").append(this.rchecksum).append(EOL);
		}
		sb.append(EOL);
		return sb.toString();
	}

} // PreambleImpl
