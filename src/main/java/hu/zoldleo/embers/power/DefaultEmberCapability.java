package hu.zoldleo.embers.power;

import hu.zoldleo.embers.api.power.IEmberCapability;

public class DefaultEmberCapability implements IEmberCapability {
	private double ember = 0;
	private double capacity = 0;

	@Override
	public double getEmber() {
		return ember;
	}

	@Override
	public double getEmberCapacity() {
		return capacity;
	}

	@Override
	public void setEmber(double value) {
		ember = value;
	}

	@Override
	public void setEmberCapacity(double value) {
		capacity = value;
	}

	@Override
	public double addAmount(double value, boolean doAdd) {
		double added = Math.min(capacity - ember,value);
		double newEmber = ember + added;
		if (doAdd){
			if(newEmber != ember)
				onContentsChanged();
			ember += added;
		}
		return added;
	}

	@Override
	public double removeAmount(double value, boolean doRemove) {
		double removed = Math.min(ember,value);
		double newEmber = ember - removed;
		if (doRemove){
			if(newEmber != ember)
				onContentsChanged();
			ember -= removed;
		}
		return removed;
	}
}