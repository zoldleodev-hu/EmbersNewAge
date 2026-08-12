package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.api.augment.IAugment;

import hu.zoldleo.embers.recipe.AnvilAugmentRecipe;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

public class AnvilAugmentRecipeBuilder {

	public ResourceLocation id;
	public Ingredient tool;
	public Ingredient input;
	public Holder<IAugment> augment;

	public static AnvilAugmentRecipeBuilder create(Holder<IAugment> augment) {
		AnvilAugmentRecipeBuilder builder = new AnvilAugmentRecipeBuilder();
		builder.augment = augment;
		builder.id = ResourceLocation.parse(augment.getRegisteredName());
		return builder;
	}

	public AnvilAugmentRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public AnvilAugmentRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public AnvilAugmentRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

    public AnvilAugmentRecipeBuilder tool(ICustomIngredient tool) {
        this.tool = tool.toVanilla();
        return this;
    }

	public AnvilAugmentRecipeBuilder tool(Ingredient tool) {
		this.tool = tool;
		return this;
	}

	public AnvilAugmentRecipeBuilder tool(ItemLike... tool) {
		tool(Ingredient.of(tool));
		return this;
	}

	public AnvilAugmentRecipeBuilder tool(TagKey<Item> tag) {
		tool(Ingredient.of(tag));
		return this;
	}

	public AnvilAugmentRecipeBuilder input(Ingredient input) {
		this.input = input;
		return this;
	}

	public AnvilAugmentRecipeBuilder input(ItemLike... input) {
		input(Ingredient.of(input));
		return this;
	}

	public AnvilAugmentRecipeBuilder input(TagKey<Item> tag) {
		input(Ingredient.of(tag));
		return this;
	}

	public AnvilAugmentRecipe build() {
		return new AnvilAugmentRecipe(tool, input, augment);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}