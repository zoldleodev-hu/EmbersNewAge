package hu.zoldleo.embers.item;

import hu.zoldleo.embers.api.power.IEmberCapability;

import hu.zoldleo.embers.api.item.IHeldEmberCell;
import hu.zoldleo.embers.power.DefaultEmberItemCapability;

import net.minecraft.world.item.ItemStack;

public class EmberCartridgeItem extends EmberStorageItem {
	public static final double CAPACITY = 6000.0;

	public EmberCartridgeItem(Properties properties) {
		super(properties);
	}

	@Override
	public double getCapacity() {
		return CAPACITY;
	}

    @Override
    public IEmberCapability getEmberCapability(ItemStack stack) {
        return new EmberCartridgeCapability(stack);
    }

	public static class EmberCartridgeCapability extends DefaultEmberItemCapability implements IHeldEmberCell {
		public EmberCartridgeCapability(ItemStack stack) {
			super(stack);
		}
	}
}