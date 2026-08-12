package hu.zoldleo.embers.recipe.base;

import java.util.Collection;
import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.context.BoringContext;
import hu.zoldleo.embers.util.WeightedItemStack;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface IBoringRecipe extends Recipe<BoringContext> {

	WeightedItemStack getOutput(BoringContext context);

	@Override
	default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.EMBER_BORE_ITEM.get());
	}

	@Override
	default @NotNull RecipeType<?> getType() {
		return RegistryManager.BORING.get();
	}

	int getMinHeight();

	int getMaxHeight();

	Collection<ResourceLocation> getDimensions();

	Collection<ResourceLocation> getBiomes();

	double getChance();

	WeightedItemStack getDisplayOutput();

	List<ItemStack> getDisplayInput();

	@Override
	@Deprecated
	default @NotNull ItemStack assemble(@NotNull BoringContext context, HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
	default boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}
