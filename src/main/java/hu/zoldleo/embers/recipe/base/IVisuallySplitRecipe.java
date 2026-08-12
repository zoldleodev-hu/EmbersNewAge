package hu.zoldleo.embers.recipe.base;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

public interface IVisuallySplitRecipe<R extends Recipe<?>> {
	List<R> getVisualRecipes();
}