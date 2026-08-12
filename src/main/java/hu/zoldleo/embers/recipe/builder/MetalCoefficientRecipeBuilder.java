package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.recipe.MetalCoefficientRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MetalCoefficientRecipeBuilder {
	public ResourceLocation id;
	public ResourceLocation blockTag;
	public double coefficient;

	public static MetalCoefficientRecipeBuilder create(ResourceLocation tag) {
		MetalCoefficientRecipeBuilder builder = new MetalCoefficientRecipeBuilder();
		builder.blockTag = tag;
		return builder;
	}

	public static MetalCoefficientRecipeBuilder create(TagKey<Block> tag) {
		MetalCoefficientRecipeBuilder builder = create(tag.location());
		builder.id = tag.location();
		return builder;
	}

	public MetalCoefficientRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public MetalCoefficientRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public MetalCoefficientRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public MetalCoefficientRecipeBuilder coefficient(double coefficient) {
		this.coefficient = coefficient;
		return this;
	}

	public MetalCoefficientRecipe build() {
		return new MetalCoefficientRecipe(BlockTags.create(blockTag), coefficient);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}