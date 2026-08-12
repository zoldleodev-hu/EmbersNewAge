package hu.zoldleo.embers.recipe.base;

import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public interface IMeltingRecipe extends Recipe<RecipeInput> {

	FluidStack getOutput(RecipeInput context);

	FluidStack getBonus();

	FluidStack process(RecipeInput context);

	@Override
    default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.MELTER_ITEM.get());
	}

	@Override
    default @NotNull RecipeType<?> getType() {
		return RegistryManager.MELTING.get();
	}

	FluidStack getDisplayOutput();

	Ingredient getDisplayInput();

	@Override
	@Deprecated
    default @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
    default @NotNull ItemStack assemble(@NotNull RecipeInput context, HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
    default boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}