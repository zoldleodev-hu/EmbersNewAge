package hu.zoldleo.embers.recipe.base;

import java.util.ArrayList;
import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.api.misc.AlchemyResult;

import hu.zoldleo.embers.recipe.context.AlchemyContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public interface IAlchemyRecipe extends Recipe<AlchemyContext> {

	ArrayList<Ingredient> getCode(long seed, ResourceLocation id);

	boolean matchesCorrect(AlchemyContext context, Level pLevel, ResourceLocation id);

	AlchemyResult getResult(AlchemyContext context, ResourceLocation id);

    ItemStack assemble(AlchemyContext context, HolderLookup.Provider registry, ResourceLocation id);

	@Override
    default @NotNull ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.ALCHEMY_TABLET_ITEM.get());
	}

	@Override
    default @NotNull RecipeType<?> getType() {
		return RegistryManager.ALCHEMY.get();
	}

	@Override
    default @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registry) {
		return getResultItem();
	}

	Ingredient getCenterInput();

	List<Ingredient> getInputs();

	List<Ingredient> getAspects();

	ItemStack getResultItem();

	ItemStack getfailureItem();

	@Override
	@Deprecated
    default boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	class PedestalContents {
		public ItemStack aspect;
		public ItemStack input;

		public PedestalContents(ItemStack aspect, ItemStack input) {
			this.aspect = aspect;
			this.input = input;
		}
	}
}