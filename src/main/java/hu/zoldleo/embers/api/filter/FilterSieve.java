package hu.zoldleo.embers.api.filter;

import java.util.List;
import java.util.Objects;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.util.FilterUtil;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FilterSieve implements IFilter {
	public static final ResourceLocation RESOURCE_LOCATION = Embers.res("sieve");

	private ItemStack stack1;
	private ItemStack stack2;
	private int offset;
	private EnumFilterSetting setting;
	private boolean inverted;
	private IFilterComparator comparator;

	public FilterSieve(CompoundTag tag, HolderLookup.Provider provider) {
		readFromNBT(tag, provider);
	}

	public FilterSieve(ItemStack stack1, ItemStack stack2, int offset, EnumFilterSetting setting, boolean inverted) {
		this.stack1 = stack1;
		this.stack2 = stack2;
		this.offset = offset;
		this.setting = setting;
		this.inverted = inverted;
		findComparator();
	}

	private void findComparator() {
		if(stack1.isEmpty() && stack2.isEmpty())
			comparator = FilterUtil.ANY;
		else {
			List<IFilterComparator> comparators = FilterUtil.getComparators(stack1, stack2);
			comparator = comparators.get(offset % comparators.size());
		}
	}

	@Override
	public ResourceLocation getType() {
		return RESOURCE_LOCATION;
	}

	@Override
	public boolean acceptsItem(ItemStack stack) {
		return comparator.isBetween(stack1, stack2, stack, setting) != inverted;
	}

	@Override
	public String formatFilter() {
		if(comparator == null)
			return "INVALID COMPARATOR";
		return comparator.format(stack1,stack2,setting,inverted);
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag tag, HolderLookup.Provider provider) {
		tag.putString("type",getType().toString());
		tag.put("stack1",stack1.saveOptional(provider));
		tag.put("stack2",stack2.saveOptional(provider));
		tag.putInt("offset",offset);
		tag.putInt("setting",setting.ordinal());
		tag.putBoolean("inverted",inverted);
		tag.putString("comparator", comparator.getName());
		return tag;
	}

	@Override
	public void readFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
		stack1 = ItemStack.parseOptional(provider, tag.getCompound("stack1"));
		stack2 = ItemStack.parseOptional(provider, tag.getCompound("stack2"));
		offset = tag.getInt("offset");
		setting = EnumFilterSetting.values()[tag.getInt("setting")];
		inverted = tag.getBoolean("invert");
		if(tag.contains("comparator"))
			comparator = FilterUtil.getComparator(tag.getString("comparator"));
		else
			findComparator();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FilterSieve)
			return equals((FilterSieve) obj);
		return super.equals(obj);
	}

	private boolean equals(FilterSieve other) {
		return Objects.equals(comparator, other.comparator)
				&& Objects.equals(inverted,other.inverted)
				&& Objects.equals(setting, other.setting)
				&& ItemStack.isSameItemSameComponents(stack1, other.stack1)
				&& ItemStack.isSameItemSameComponents(stack2, other.stack2);
	}
}