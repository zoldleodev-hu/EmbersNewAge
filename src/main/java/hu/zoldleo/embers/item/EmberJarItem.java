package hu.zoldleo.embers.item;

import hu.zoldleo.embers.api.power.IEmberCapability;

import hu.zoldleo.embers.api.item.IHeldEmberCell;
import hu.zoldleo.embers.api.item.IInventoryEmberCell;
import hu.zoldleo.embers.power.DefaultEmberItemCapability;

import net.minecraft.world.item.ItemStack;

public class EmberJarItem extends EmberStorageItem {
	public static final double CAPACITY = 2000.0;

	public EmberJarItem(Properties properties) {
		super(properties);
	}

	@Override
	public double getCapacity() {
		return CAPACITY;
	}

    @Override
    public IEmberCapability getEmberCapability(ItemStack stack) {
        return new EmberJarCapability(stack);
    }

	public static class EmberJarCapability extends DefaultEmberItemCapability implements IInventoryEmberCell, IHeldEmberCell {
		public EmberJarCapability(ItemStack stack) {
			super(stack);
		}
	}
}