package hu.zoldleo.embers.api.filter;

import net.minecraft.world.item.ItemStack;

public abstract class ComparatorMatch implements IFilterComparator {
	private final String name;
	private final int priority;

	public ComparatorMatch(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getCompare(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean isBetween(ItemStack stack1, ItemStack stack2, ItemStack testStack, EnumFilterSetting setting) {
		return match(stack1,testStack);
	}
}