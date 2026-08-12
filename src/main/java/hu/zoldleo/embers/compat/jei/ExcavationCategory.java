package hu.zoldleo.embers.compat.jei;

import hu.zoldleo.embers.Embers;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IBoringRecipe;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExcavationCategory extends BoringCategory {
	public static Component title = Component.translatable(Embers.MODID + ".jei.recipe.excavation");

	public ExcavationCategory(IGuiHelper helper) {
		super(helper);
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RegistryManager.EXCAVATION_BUCKETS_ITEM.get()));
	}

	@Override
	public @NotNull RecipeType<IBoringRecipe> getRecipeType() {
		return JEIPlugin.EXCAVATION;
	}

	@Override
	public @NotNull Component getTitle() {
		return title;
	}
}