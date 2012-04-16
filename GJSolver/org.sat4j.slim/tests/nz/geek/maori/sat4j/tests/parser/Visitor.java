package nz.geek.maori.sat4j.tests.parser;

public interface Visitor {

	/**
	 * Int array in dimacs format
	 */
	public void addClause(int[] clause);
}
