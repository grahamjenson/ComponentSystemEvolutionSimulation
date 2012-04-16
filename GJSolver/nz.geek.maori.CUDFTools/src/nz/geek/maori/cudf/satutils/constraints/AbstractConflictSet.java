package nz.geek.maori.cudf.satutils.constraints;

import java.util.ArrayList;
import java.util.List;

public class AbstractConflictSet implements AbstractConstraint{

	private ArrayList<Literal> lits = new ArrayList<Literal>();
	
	public AbstractConflictSet() {
		
	}
	
	public AbstractConflictSet(ArrayList<Literal> lits) {
		super();
		this.lits.addAll(lits);
	}

	public List<Literal> getLits() {
		return lits;
	}
}
