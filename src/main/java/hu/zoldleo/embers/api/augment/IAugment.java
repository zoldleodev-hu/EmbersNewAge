package hu.zoldleo.embers.api.augment;

import net.minecraft.world.item.ItemStack;

public interface IAugment {
	double getCost();

	default boolean countTowardsTotalLevel() {
		return true;
	}

	default boolean canRemove() {
		return true;
	}

	default boolean shouldRenderTooltip() {
		return true;
	}

	default void onApply(ItemStack stack) {
		//NOOP
	}

	default void onRemove(ItemStack stack) {
		//NOOP
	}
}