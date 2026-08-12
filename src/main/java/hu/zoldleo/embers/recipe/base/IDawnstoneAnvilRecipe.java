package hu.zoldleo.embers.recipe.base;

import java.util.List;

import hu.zoldleo.embers.RegistryManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface IDawnstoneAnvilRecipe extends Recipe<RecipeInput> {

	List<ItemStack> getOutput(RecipeInput context);

	@Override
	default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.DAWNSTONE_ANVIL_ITEM.get());
	}

	@Override
	default @NotNull RecipeType<?> getType() {
		return RegistryManager.DAWNSTONE_ANVIL_RECIPE.get();
	}

	List<ItemStack> getDisplayInputBottom();

	List<ItemStack> getDisplayInputTop();

	List<ItemStack> getDisplayOutput();


	@Override
	@Deprecated
	default @NotNull ItemStack assemble(@NotNull RecipeInput context, HolderLookup.@NotNull Provider pRegistryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
	default @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider pRegistryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	@Deprecated
	default boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}
