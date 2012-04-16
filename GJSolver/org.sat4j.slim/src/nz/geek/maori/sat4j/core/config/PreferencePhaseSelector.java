package nz.geek.maori.sat4j.core.config;

import static nz.geek.maori.sat4j.tools.LiteralsUtils.negLit;
import static nz.geek.maori.sat4j.tools.LiteralsUtils.posLit;

import java.util.Map;

import nz.geek.maori.sat4j.specs.IPhaseSelectionStrategy;

public class PreferencePhaseSelector implements IPhaseSelectionStrategy {

	//uses map so as not to prefeer both positive and negative
	Map<Integer,Boolean> prefs;

	public PreferencePhaseSelector(Map<Integer,Boolean> prefs) {
		this.prefs = prefs;
	}
	
	protected int[] phase;

	@Override
	public void init(int nlength) {
		this.phase = new int[nlength];
		for (int i = 1; i < nlength; i++) {
			if(prefs.get(i) != null){
				if(prefs.get(i))
				{
					this.phase[i] = posLit(i);
				}
				else
				{
					this.phase[i] =negLit(i);
				}
			}
			else
			{
				this.phase[i] = negLit(i);
			}
		}
	}

	@Override
	public void init(int var, int p) {
		this.phase[var] = p;
	}

	@Override
	public int select(int var) {
		return this.phase[var];
	}

	@Override
	public void assignLiteral(int p) {
		//this.phase[p >> 1] = p;
	}

	@Override
	public String toString() {
		return "Prefeered Phase Selection";
	}

	@Override
	public void updateVar(int p) {
		//phase[p >> 1] = p;
	}
}
