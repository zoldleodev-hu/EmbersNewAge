package hu.zoldleo.embers.recipe.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.BoringRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;

public class BoringRecipeBuilder {

	public ResourceLocation id;
	public ItemStack result;
	public int weight = 20;
	public int minHeight = Integer.MIN_VALUE;
	public int maxHeight = Integer.MAX_VALUE;
	public List<ResourceLocation> dimensions = new ArrayList<>();
	public List<ResourceLocation> biomes = new ArrayList<>();
	public TagKey<Block> requiredBlock = null;
	public int amountRequired = 0;
	public double chance = -1.0;
	public RecipeSerializer<?> type = RegistryManager.BORING_SERIALIZER.get();

	public static BoringRecipeBuilder create(ItemStack itemStack) {
		BoringRecipeBuilder builder = new BoringRecipeBuilder();
		builder.result = itemStack;
		builder.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return builder;
	}

	public static BoringRecipeBuilder create(Item item) {
		return create(new ItemStack(item));
	}

	public BoringRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public BoringRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public BoringRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public BoringRecipeBuilder type(RecipeSerializer<?> type) {
		this.type = type;
		return this;
	}

	public BoringRecipeBuilder weight(int weight) {
		this.weight = weight;
		return this;
	}

	public BoringRecipeBuilder minHeight(int minHeight) {
		this.minHeight = minHeight;
		return this;
	}

	public BoringRecipeBuilder maxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
		return this;
	}

	public BoringRecipeBuilder dimensions(List<ResourceLocation> dimensions) {
		this.dimensions = dimensions;
		return this;
	}

	public BoringRecipeBuilder biomes(List<ResourceLocation> biomes) {
		this.biomes = biomes;
		return this;
	}

	public BoringRecipeBuilder dimension(ResourceLocation... dimensions) {
        this.dimensions.addAll(Arrays.asList(dimensions));
		return this;
	}

	public BoringRecipeBuilder biome(ResourceLocation... biomes) {
        this.biomes.addAll(Arrays.asList(biomes));
		return this;
	}

	public BoringRecipeBuilder require(TagKey<Block> requiredBlock, int amountRequired) {
		this.requiredBlock = requiredBlock;
		this.amountRequired = amountRequired;
		return this;
	}

	public BoringRecipeBuilder chance(double chance) {
		this.chance = chance;
		return this;
	}

	public BoringRecipe build() {
		return new BoringRecipe(result, weight, minHeight, maxHeight, dimensions, biomes, requiredBlock, amountRequired, chance);
	}

	public void save(RecipeOutput output) {
		output.accept(id, build(), null);
	}
}