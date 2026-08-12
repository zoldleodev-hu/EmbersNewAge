package hu.zoldleo.embers.recipe.base;

import java.util.List;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.recipe.context.MixingContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

public interface IMixingRecipe extends Recipe<MixingContext> {

	FluidStack getOutput(MixingContext context);

	FluidStack process(MixingContext context);

	@Override
    default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.MIXER_CENTRIFUGE_ITEM.get());
	}

	@Override
    default @NotNull RecipeType<?> getType() {
		return RegistryManager.MIXING.get();
	}

	List<SizedFluidIngredient> getDisplayInputFluids();

	FluidStack getDisplayOutput();

	@Override
	@Deprecated
    default @NotNull ItemStack assemble(@NotNull MixingContext context, HolderLookup.@NotNull Provider registry) {
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
