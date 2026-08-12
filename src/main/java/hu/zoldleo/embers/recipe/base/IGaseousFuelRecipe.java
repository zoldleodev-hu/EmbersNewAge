package hu.zoldleo.embers.recipe.base;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.recipe.context.FluidHandlerContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

public interface IGaseousFuelRecipe extends Recipe<FluidHandlerContext> {

	int getBurnTime(FluidHandlerContext context);

	double getPowerMultiplier(FluidHandlerContext context);

	int process(FluidHandlerContext context, int amount);

	@Override
	default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.WILDFIRE_STIRLING_ITEM.get());
	}

	@Override
	default @NotNull RecipeType<?> getType() {
		return RegistryManager.GASEOUS_FUEL.get();
	}

    SizedFluidIngredient getDisplayInput();

	int getDisplayBurnTime();

	double getDisplayMultiplier();

	@Override
	@Deprecated
	default @NotNull ItemStack assemble(@NotNull FluidHandlerContext context, HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
	default @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
    default boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}