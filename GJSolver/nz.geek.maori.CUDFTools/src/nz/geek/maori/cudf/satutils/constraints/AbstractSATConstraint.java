package nz.geek.maori.cudf.satutils.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractSATConstraint implements AbstractConstraint{

	private ArrayList<Literal> lits = new ArrayList<Literal>();
	
	public AbstractSATConstraint() {
		
	}
	
	public AbstractSATConstraint(Collection<Literal> lits) {
		super();
		this.lits.addAll(lits);
	}

	public List<Literal> getLits() {
		return lits;
	}

}
