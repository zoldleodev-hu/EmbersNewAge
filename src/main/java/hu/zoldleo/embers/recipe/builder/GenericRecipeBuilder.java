package hu.zoldleo.embers.recipe.builder;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class GenericRecipeBuilder {
	public Recipe<?> recipe;
    public ResourceLocation id;

	public static GenericRecipeBuilder create(ResourceLocation id, Recipe<?> recipe) {
		GenericRecipeBuilder builder = new GenericRecipeBuilder();
        builder.id = id;
		builder.recipe = recipe;
		return builder;
	}

	public void save(RecipeOutput output) {
        output.accept(id, recipe, null); // TODO
	}
}