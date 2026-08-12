package hu.zoldleo.embers.recipe.base;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.recipe.context.StampingContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public interface IStampingRecipe extends Recipe<StampingContext> {

	ItemStack getOutput(RecipeWrapper context);

	@Override
    default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.STAMPER_ITEM.get());
	}

	@Override
    default @NotNull RecipeType<?> getType() {
		return RegistryManager.STAMPING.get();
	}

	@Override
    default @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
		return getResultItem();
	}

	ItemStack getResultItem();

    SizedFluidIngredient getDisplayInputFluid();

	Ingredient getDisplayInput();

	Ingredient getDisplayStamp();

	@Override
	@Deprecated
	default boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}
