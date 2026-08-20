package hu.zoldleo.embers.recipe;

import com.mojang.serialization.MapCodec;
import hu.zoldleo.embers.util.ItemStackNonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.item.IInflictorGemHolder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class GemUnsocketRecipe implements CraftingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	@Override
	public boolean matches(CraftingInput container, @NotNull Level level) {
        if (container.size() != 1)
            return false;
        ItemStack stack = container.getItem(0);
        return stack.getItem() instanceof IInflictorGemHolder gemItem && !gemItem.isEmpty(stack);
	}

	@Override
	public @NotNull ItemStack assemble(CraftingInput container, HolderLookup.@NotNull Provider registryAccess) {
		ItemStack capeStack = container.getItem(0).copy();
        IInflictorGemHolder gemItem = (IInflictorGemHolder)capeStack.getItem();
        gemItem.detachGem(capeStack);
		return capeStack;
	}

	@Override
	public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
		for (int i = 0; i < container.size(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof IInflictorGemHolder gemItem)
                return ItemStackNonNullList.of(gemItem.getLastGem(stack));
		}
        return ItemStackNonNullList.of();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 1;
	}

	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registryAccess) {
		return new ItemStack(RegistryManager.ASHEN_CLOAK.get());
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public @NotNull CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
	}

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GemUnsocketRecipe;
    }

	public static class Serializer implements RecipeSerializer<GemUnsocketRecipe> {
        public static final MapCodec<GemUnsocketRecipe> CODEC = MapCodec.unit(new GemUnsocketRecipe());
        public static final StreamCodec<RegistryFriendlyByteBuf, GemUnsocketRecipe> STREAM_CODEC = StreamCodec.unit(new GemUnsocketRecipe());

        @Override
        public @NotNull MapCodec<GemUnsocketRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, GemUnsocketRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}