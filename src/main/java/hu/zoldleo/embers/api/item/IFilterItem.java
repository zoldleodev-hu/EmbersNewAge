package hu.zoldleo.embers.api.item;

import hu.zoldleo.embers.api.filter.IFilter;

import net.minecraft.world.item.ItemStack;

public interface IFilterItem {
	IFilter getFilter(ItemStack stack);
}