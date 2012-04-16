package nz.geek.maori.cudf.satutils.constraints;

public class Literal {

	@Override
	public String toString() {
		return phase ? "+" + variable.toString() :"-" + variable.toString(); 
	}

	private boolean phase = true;
	private Object variable;
	

	public boolean isPhase() {
		return phase;
	}
	
	public Object getVariable() {
		return variable;
	}
	
	public Literal(boolean phase, Object variable) {
		super();
		this.phase = phase;
		this.variable = variable;
	}
	

	public static Literal POS(Object var)
	{
		return new Literal(true,var);
	}
	
	public static Literal NEG(Object var)
	{
		return new Literal(false,var);
	}
}
