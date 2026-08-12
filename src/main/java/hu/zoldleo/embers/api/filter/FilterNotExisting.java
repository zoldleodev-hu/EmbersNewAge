package hu.zoldleo.embers.api.filter;

import hu.zoldleo.embers.Embers;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class FilterNotExisting extends FilterExisting {
    public static final ResourceLocation RESOURCE_LOCATION = Embers.res("not_existing");

    @Override
    public ResourceLocation getType() {
        return RESOURCE_LOCATION;
    }

    @Override
    public boolean acceptsItem(ItemStack stack, IItemHandler itemHandler) {
        return !super.acceptsItem(stack, itemHandler);
    }

    @Override
    public String formatFilter() {
        return I18n.get(Embers.MODID + ".filter.not_existing");
    }
}