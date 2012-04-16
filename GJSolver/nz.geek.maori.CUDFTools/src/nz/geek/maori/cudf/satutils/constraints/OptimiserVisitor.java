package nz.geek.maori.cudf.satutils.constraints;

import java.util.Collection;

public interface OptimiserVisitor {

	public Model getModel();
	
	public boolean tryConstraints(Collection<AbstractConstraint> cons);
	
	public boolean addNonConflistingConstraints(Collection<AbstractConstraint> cons);
	
	public boolean isTimeout();
	
	public void updateOrder();
	
	public void clear();
}
