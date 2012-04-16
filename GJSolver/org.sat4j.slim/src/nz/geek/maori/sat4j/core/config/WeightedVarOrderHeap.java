package nz.geek.maori.sat4j.core.config;

import java.util.Map;

import nz.geek.maori.sat4j.specs.IPhaseSelectionStrategy;


public class WeightedVarOrderHeap extends VarOrderHeap {


	Map<Integer,Double> weights = null;
	public WeightedVarOrderHeap(IPhaseSelectionStrategy strategy, Map<Integer,Double> weights) {
		super(strategy);
		this.weights = weights;
	}


	@Override
	public void init() {
		super.init();

		for (int var : weights.keySet()) {
			double w = weights.get(var);
			activity[var] = w;
			if (heap.inHeap(var))
				heap.increase(var);
			else
				heap.insert(var);
			//phaseStrategy.init(var, neg(p));

		}

	}


}
