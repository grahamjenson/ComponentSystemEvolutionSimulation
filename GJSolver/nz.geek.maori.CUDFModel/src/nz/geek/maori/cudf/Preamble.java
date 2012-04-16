/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf;

import java.util.Map;

public interface Preamble {
	String getUchecksum();

	void setUchecksum(String value);

	String getSchecksum();

	void setSchecksum(String value);

	String getRchecksum();

	void setRchecksum(String value);

	Map<String, Type> getTypes();

	Map<String, Object> getDefaults();

	String toCUDF();

} // Preamble
