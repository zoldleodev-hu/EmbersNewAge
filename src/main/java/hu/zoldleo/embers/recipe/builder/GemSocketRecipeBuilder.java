package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.recipe.GemSocketRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class GemSocketRecipeBuilder {

	public ResourceLocation id;
	public Ingredient ingredient;

	public static GemSocketRecipeBuilder create(Ingredient ingredient) {
		GemSocketRecipeBuilder builder = new GemSocketRecipeBuilder();
		builder.ingredient = ingredient;
		return builder;
	}

	public static GemSocketRecipeBuilder create(TagKey<Item> tag) {
		GemSocketRecipeBuilder builder = create(Ingredient.of(tag));
		builder.id = tag.location();
		return builder;
	}

	public static GemSocketRecipeBuilder create(ItemStack itemStack) {
		GemSocketRecipeBuilder builder = create(Ingredient.of(itemStack));
		builder.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return builder;
	}

	public static GemSocketRecipeBuilder create(Item item) {
		return create(new ItemStack(item));
	}

	public GemSocketRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public GemSocketRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public GemSocketRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public GemSocketRecipe build() {
		return new GemSocketRecipe(ingredient);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}