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

public enum Type {

	BOOL(0, "BOOL", "bool"),

	INT(1, "INT", "int"),

	ENUM(2, "ENUM", "enum[]"),

	STRING(3, "STRING", "string"),

	PACKAGELIST(4, "PACKAGELIST", "vpkglist"),

	PACKAGEFORMULA(5, "PACKAGEFORMULA", "vpkgformula"),

	PACKAGE(6, "PACKAGE", "vpkg");

	public static final int BOOL_VALUE = 0;

	public static final int INT_VALUE = 1;

	public static final int ENUM_VALUE = 2;

	public static final int STRING_VALUE = 3;

	public static final int PACKAGELIST_VALUE = 4;

	public static final int PACKAGEFORMULA_VALUE = 5;

	public static final int PACKAGE_VALUE = 6;

	private static final Type[] VALUES_ARRAY = new Type[] { BOOL, INT, ENUM,
			STRING, PACKAGELIST, PACKAGEFORMULA, PACKAGE, };

	public static final List<Type> VALUES = Collections.unmodifiableList(Arrays
			.asList(VALUES_ARRAY));

	public static Type get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Type result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	public static Type getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Type result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	public static Type get(int value) {
		switch (value) {
		case BOOL_VALUE:
			return BOOL;
		case INT_VALUE:
			return INT;
		case ENUM_VALUE:
			return ENUM;
		case STRING_VALUE:
			return STRING;
		case PACKAGELIST_VALUE:
			return PACKAGELIST;
		case PACKAGEFORMULA_VALUE:
			return PACKAGEFORMULA;
		case PACKAGE_VALUE:
			return PACKAGE;
		}
		return null;
	}

	private final int value;

	private final String name;

	private final String literal;

	private Type(int value, String name, String literal) {
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

	public static String toCUDF(Object prop) {
		StringBuffer sb = new StringBuffer();
		if (prop instanceof PackageList) {
			sb.append(((PackageList) prop).toCUDF());
		} else if (prop instanceof PackageVersionConstraint) {
			sb.append(((PackageVersionConstraint) prop).toCUDF());
		} else if (prop instanceof PackageFormula) {
			sb.append(((PackageFormula) prop).toCUDF());
		} else {
			sb.append(prop);
		}

		return sb.toString();
	}

} // Type
