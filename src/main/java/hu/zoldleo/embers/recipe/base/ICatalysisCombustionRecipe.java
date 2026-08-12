package hu.zoldleo.embers.recipe.base;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.recipe.context.CatalysisCombustionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface ICatalysisCombustionRecipe extends Recipe<CatalysisCombustionContext> {

	int getBurnTIme(CatalysisCombustionContext context);

	double getmultiplier(CatalysisCombustionContext context);

	int process(CatalysisCombustionContext context);

	@Override
	default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.IGNEM_REACTOR_ITEM.get());
	}

	@Override
	default @NotNull RecipeType<?> getType() {
		return RegistryManager.CATALYSIS_COMBUSTION.get();
	}

	Ingredient getDisplayInput();

	Ingredient getDisplayMachine();

	int getDisplayTime();

	double getDisplayMultiplier();

	@Override
	@Deprecated
	default @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
	default @NotNull ItemStack assemble(@NotNull CatalysisCombustionContext context, HolderLookup.@NotNull Provider registry) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
	default boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}
