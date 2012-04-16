package nz.geek.maori.cudf.satutils.constraints;

import java.util.ArrayList;

public class AbstractPBConstraint implements AbstractConstraint{

	private ArrayList<Long> coefs;
	private ArrayList<Literal> lits;
	private boolean moreThan;
	private long degree;

	public ArrayList<Long> getCoefs() {
		return this.coefs;
	}

	public ArrayList<Literal> getLits() {
		return this.lits;
	}

	public boolean isMoreThan() {
		return this.moreThan;
	}

	public long getDegree() {
		return this.degree;
	}

	public AbstractPBConstraint(ArrayList<Literal> lits, ArrayList<Long> coefs,
			boolean moreThan, long degree) {
		super();
		assert lits.size() == coefs.size();

		this.lits = lits;
		this.coefs = coefs;
		this.moreThan = moreThan;
		this.degree = degree;
	}

}
