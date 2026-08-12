package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.recipe.EmberActivationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class EmberActivationRecipeBuilder {
	public ResourceLocation id;
	public Ingredient ingredient;
	public int ember;

	public static EmberActivationRecipeBuilder create(Ingredient ingredient) {
		EmberActivationRecipeBuilder builder = new EmberActivationRecipeBuilder();
		builder.ingredient = ingredient;
		return builder;
	}

	public static EmberActivationRecipeBuilder create(TagKey<Item> tag) {
		EmberActivationRecipeBuilder builder = create(Ingredient.of(tag));
		builder.id = tag.location();
		return builder;
	}

	public static EmberActivationRecipeBuilder create(ItemStack itemStack) {
		EmberActivationRecipeBuilder builder = create(Ingredient.of(itemStack));
		builder.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return builder;
	}

	public static EmberActivationRecipeBuilder create(Item item) {
		return create(new ItemStack(item));
	}

	public EmberActivationRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public EmberActivationRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public EmberActivationRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public EmberActivationRecipeBuilder ember(int ember) {
		this.ember = ember;
		return this;
	}

	public EmberActivationRecipe build() {
		return new EmberActivationRecipe(ingredient, ember);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}