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

public enum Relation {
	GEQ(0, "GEQ", ">="),

	G(1, "G", ">"),

	L(2, "L", "<"),

	LEQ(3, "LEQ", "<="),

	EQUALS(4, "EQUALS", "="),

	NEQ(5, "NEQ", "!=");

	public static final int GEQ_VALUE = 0;

	public static final int G_VALUE = 1;

	public static final int L_VALUE = 2;

	public static final int LEQ_VALUE = 3;

	public static final int EQUALS_VALUE = 4;

	public static final int NEQ_VALUE = 5;

	private static final Relation[] VALUES_ARRAY = new Relation[] { GEQ, G, L,
			LEQ, EQUALS, NEQ, };

	public static final List<Relation> VALUES = Collections
			.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	public static Relation get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Relation result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	public static Relation getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Relation result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	public static Relation get(int value) {
		switch (value) {
		case GEQ_VALUE:
			return GEQ;
		case G_VALUE:
			return G;
		case L_VALUE:
			return L;
		case LEQ_VALUE:
			return LEQ;
		case EQUALS_VALUE:
			return EQUALS;
		case NEQ_VALUE:
			return NEQ;
		}
		return null;
	}

	private final int value;

	private final String name;

	private final String literal;

	private Relation(int value, String name, String literal) {
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

} // Relation
