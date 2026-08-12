package hu.zoldleo.embers.augment;

import hu.zoldleo.embers.api.augment.IAugment;

public class AugmentBase implements IAugment {
	double cost;

	public AugmentBase(double cost) {
		this.cost = cost;
	}

	@Override
	public double getCost() {
		return cost;
	}
}