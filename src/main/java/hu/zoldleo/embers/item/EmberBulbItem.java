package hu.zoldleo.embers.item;

import hu.zoldleo.embers.api.power.IEmberCapability;

import net.minecraft.world.item.ItemStack;

public class EmberBulbItem extends EmberStorageItem implements IEmbersCurioItem {

	public static final double CAPACITY = 1000.0;

	public EmberBulbItem(Properties properties) {
		super(properties);
	}

	@Override
	public double getCapacity() {
		return CAPACITY;
	}

    @Override
    public IEmberCapability getEmberCapability(ItemStack stack) {
        return new EmberJarItem.EmberJarCapability(stack);
    }
}