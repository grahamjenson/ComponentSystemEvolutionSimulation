/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Keep {
	NONE(0, "NONE", "none"),

	VERSION(1, "VERSION", "version"),

	PACKAGE(2, "PACKAGE", "package"),

	FEATURE(3, "FEATURE", "feature");

	public static final int NONE_VALUE = 0;

	public static final int VERSION_VALUE = 1;

	public static final int PACKAGE_VALUE = 2;

	public static final int FEATURE_VALUE = 3;

	private static final Keep[] VALUES_ARRAY = new Keep[] { NONE, VERSION,
			PACKAGE, FEATURE, };

	public static final List<Keep> VALUES = Collections.unmodifiableList(Arrays
			.asList(VALUES_ARRAY));

	public static Keep get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Keep result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	public static Keep getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Keep result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	public static Keep get(int value) {
		switch (value) {
		case NONE_VALUE:
			return NONE;
		case VERSION_VALUE:
			return VERSION;
		case PACKAGE_VALUE:
			return PACKAGE;
		case FEATURE_VALUE:
			return FEATURE;
		}
		return null;
	}

	private final int value;

	private final String name;

	private final String literal;

	private Keep(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	public int getValue() {
		return this.value;
	}

	public String getName() {
		return this.name;
	}

	public String getLiteral() {
		return this.literal;
	}

	@Override
	public String toString() {
		return this.literal;
	}

} // Preserve
