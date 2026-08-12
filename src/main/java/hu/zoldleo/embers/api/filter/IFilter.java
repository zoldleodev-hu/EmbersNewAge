package hu.zoldleo.embers.api.filter;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public interface IFilter {
	ResourceLocation getType();

	boolean acceptsItem(ItemStack stack);

	default boolean acceptsItem(ItemStack stack, IItemHandler handler) {
		return acceptsItem(stack);
	}

	String formatFilter();

	CompoundTag writeToNBT(CompoundTag tag, HolderLookup.Provider provider);

	void readFromNBT(CompoundTag tag, HolderLookup.Provider provider);
}