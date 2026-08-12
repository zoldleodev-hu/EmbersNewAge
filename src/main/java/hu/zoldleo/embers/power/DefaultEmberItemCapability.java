package hu.zoldleo.embers.power;

import javax.annotation.Nonnull;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.datacomponents.EmberComponent;

import hu.zoldleo.embers.api.power.IEmberCapability;

import net.minecraft.world.item.ItemStack;

public class DefaultEmberItemCapability implements IEmberCapability {
	@Nonnull
	public ItemStack stack;
	double ember = 0;
	double capacity = 0;

	public DefaultEmberItemCapability(@Nonnull ItemStack stack) {
		this.stack = stack;
        EmberComponent component = stack.get(RegistryManager.EMBER_COMPONENT);
        if (component == null)
            return;
		setEmberCapacity(component.capacity());
        setEmber(component.ember());
	}

	@Override
	public double getEmber() {
		/*/if (stack.isEmpty())
			return 0;
        EmberComponent component = stack.get(RegistryManager.EMBER_COMPONENT);
		return component != null ? component.ember() : 0;*/
        return ember;
	}

	@Override
	public double getEmberCapacity() {
		/*/if (stack.isEmpty())
			return 0;
        EmberComponent component = stack.get(RegistryManager.EMBER_COMPONENT);
        return component != null ? component.capacity() : 0;*/
        return capacity;
	}

	@Override
	public void setEmber(double value) {
		/*/ember = value;
        EmberComponent component = stack.get(RegistryManager.EMBER_COMPONENT);
        if (component == null)
            return;
        stack.set(RegistryManager.EMBER_COMPONENT, new EmberComponent(value, component.capacity()));*/
        ember = value;
        stack.set(RegistryManager.EMBER_COMPONENT, new EmberComponent(ember, capacity));
	}

	@Override
	public void setEmberCapacity(double value) {
		/*/capacity = value;
        EmberComponent component = stack.get(RegistryManager.EMBER_COMPONENT);
        if (component == null)
            return;
        stack.set(RegistryManager.EMBER_COMPONENT, new EmberComponent(component.ember(), value));*/
        capacity = value;
        stack.set(RegistryManager.EMBER_COMPONENT, new EmberComponent(ember, capacity));
	}

	@Override
	public double addAmount(double value, boolean doAdd) {
		double ember = getEmber();
		double capacity = getEmberCapacity();
		double added = Math.min(capacity - ember, value);
		double newEmber = ember + added;
		if (doAdd) {
			if (newEmber != ember)
				onContentsChanged();
			setEmber(ember + added);
		}
		return added;
	}

	@Override
	public double removeAmount(double value, boolean doRemove) {
		double ember = getEmber();
		double removed = Math.min(ember, value);
		double newEmber = ember - removed;
		if (doRemove) {
			if (newEmber != ember)
				onContentsChanged();
			setEmber(ember - removed);
		}
		return removed;
	}
}