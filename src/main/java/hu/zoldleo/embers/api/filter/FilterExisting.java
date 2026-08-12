package hu.zoldleo.embers.api.filter;

import hu.zoldleo.embers.Embers;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class FilterExisting implements IFilter {
	public static final ResourceLocation RESOURCE_LOCATION = Embers.res("existing");

	@Override
	public ResourceLocation getType() {
		return RESOURCE_LOCATION;
	}

	@Override
	public boolean acceptsItem(ItemStack stack) {
		return false;
	}

	@Override
	public boolean acceptsItem(ItemStack stack, IItemHandler itemHandler) {
		if(itemHandler != null)
			for (int i = 0; i < itemHandler.getSlots(); i++) {
                if (itemHandler.insertItem(i, stack, true).isEmpty())
                    return true;
				/*/ItemStack slotStack = itemHandler.getStackInSlot(i);
				if (ItemHandlerHelper.canItemStacksStack(slotStack, stack))
					return true;*/
			}
		return false;
	}

	@Override
	public String formatFilter() {
		return I18n.get(Embers.MODID + ".filter.existing");
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
		tag.putString("type",getType().toString());
		return tag;
	}

	@Override
	public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
	}
}