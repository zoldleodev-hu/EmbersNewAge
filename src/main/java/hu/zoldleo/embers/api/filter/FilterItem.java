package hu.zoldleo.embers.api.filter;

import hu.zoldleo.embers.Embers;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FilterItem implements IFilter {
	public static final ResourceLocation RESOURCE_LOCATION = Embers.res("item");

	private ItemStack filterItem;

	public FilterItem(ItemStack filterItem) {
		this.filterItem = filterItem;
	}

	public FilterItem(CompoundTag tag, HolderLookup.Provider provider) {
		readFromNBT(tag, provider);
	}

	@Override
	public ResourceLocation getType() {
		return RESOURCE_LOCATION;
	}

	@Override
	public boolean acceptsItem(ItemStack stack) {
		return filterItem.getItem() == stack.getItem() && filterItem.getDamageValue() == stack.getDamageValue();
	}

	@Override
	public String formatFilter() {
		return I18n.get(Embers.MODID + ".filter.strict", filterItem.getDisplayName());
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
		tag.putString("type", getType().toString());
		tag.put("filterStack", filterItem.saveOptional(provider));
		return tag;
	}

	@Override
	public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        filterItem = ItemStack.parseOptional(provider, tag.getCompound("filterStack"));
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FilterItem)
			return equals((FilterItem) obj);
		return super.equals(obj);
	}

	private boolean equals(FilterItem other) {
		return ItemStack.isSameItemSameComponents(filterItem, other.filterItem);
	}
}