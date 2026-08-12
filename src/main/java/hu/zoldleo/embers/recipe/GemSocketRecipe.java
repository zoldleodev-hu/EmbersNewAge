package hu.zoldleo.embers.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.item.IInflictorGem;
import hu.zoldleo.embers.api.item.IInflictorGemHolder;
import hu.zoldleo.embers.util.ItemStackNonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GemSocketRecipe implements CraftingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public final Ingredient ingredient;

	public GemSocketRecipe(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	public boolean matches(CraftingInput container, @NotNull Level level) {
		ItemStack cloak = ItemStack.EMPTY;
		int cloaks = 0;
		int strings = 0;
		int gems = 0;
		for (int i = 0; i < container.size(); i ++) {
			ItemStack stack = container.getItem(i);
			if (stack.getItem() instanceof IInflictorGemHolder gemHolder && gemHolder.getAttachedGemCount(stack) == 0) {
				cloak = stack;
                break;
            }
		}
		for (int i = 0; i < container.size(); i ++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof IInflictorGemHolder) {
					cloaks++;
				} else if (ingredient.test(stack)) {
					strings++;
				} else if (!cloak.isEmpty() && ((IInflictorGemHolder) cloak.getItem()).canAttachGem(cloak, stack)) {
					gems++;
				} else {
					return false;
				}
			}
		}
		return !cloak.isEmpty() && cloaks == 1 && strings == 1 && gems > 0 && gems <= ((IInflictorGemHolder)cloak.getItem()).getGemSlots(cloak);
	}

	@Override
	public @NotNull ItemStack assemble(@NotNull CraftingInput container, HolderLookup.@NotNull Provider registryAccess) {
		ItemStack capeStack = ItemStack.EMPTY;
		for (int i = 0; i < container.size(); i++)
			if (!container.getItem(i).isEmpty() && container.getItem(i).getItem() instanceof IInflictorGemHolder) {
				capeStack = container.getItem(i).copy();
                break;
            }
		if (!capeStack.isEmpty()) {
			int counter = 0;
			for (int i = 0; i < container.size(); i ++) {
				ItemStack stack = container.getItem(i);
				if (!stack.isEmpty() && stack.getItem() instanceof IInflictorGem) {
					((IInflictorGemHolder)capeStack.getItem()).attachGem(capeStack, stack, counter);
					counter++;
				}
			}
			return capeStack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 3;
	}

	@Override
	public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registryAccess) {
		return new ItemStack(RegistryManager.ASHEN_CLOAK.get());
	}

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> nonnulllist = ItemStackNonNullList.withSize(input.size());

        ItemStack capeStack = ItemStack.EMPTY;
        for (ItemStack stack : input.items())
            if (stack.getItem() instanceof IInflictorGemHolder) {
                capeStack = stack;
                break;
            }

        int counter = 0;
        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack item = input.getItem(i);
            if (item.hasCraftingRemainingItem())
                nonnulllist.set(i, item.getCraftingRemainingItem());
            else if (!capeStack.isEmpty() && item.getItem() instanceof IInflictorGem) {
                if (!((IInflictorGemHolder)capeStack.getItem()).canAttachGem(capeStack, item, counter))
                    nonnulllist.set(i, item);
                counter++;
            }
        }

        return nonnulllist;
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

	public static class Serializer implements RecipeSerializer<GemSocketRecipe> {
        private static final MapCodec<GemSocketRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient)
        ).apply(instance, GemSocketRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, GemSocketRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.ingredient,
                GemSocketRecipe::new
        );

        @Override
        public @NotNull MapCodec<GemSocketRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, GemSocketRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}