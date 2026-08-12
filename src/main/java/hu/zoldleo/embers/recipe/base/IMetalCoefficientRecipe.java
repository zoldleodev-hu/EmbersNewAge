package hu.zoldleo.embers.recipe.base;

import java.util.List;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.recipe.context.BlockStateContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface IMetalCoefficientRecipe extends Recipe<BlockStateContext> {
	double getCoefficient(BlockStateContext context);

	@Override
    default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.PRESSURE_REFINERY_ITEM.get());
	}

	@Override
    default @NotNull RecipeType<?> getType() {
		return RegistryManager.METAL_COEFFICIENT.get();
	}

	List<ItemStack> getDisplayInput();

	double getDisplayCoefficient();

	@Override
	@Deprecated
    default @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
    default @NotNull ItemStack assemble(@NotNull BlockStateContext context, HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
    default boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}