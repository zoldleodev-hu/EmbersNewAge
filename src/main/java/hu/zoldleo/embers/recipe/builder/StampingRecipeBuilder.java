package hu.zoldleo.embers.recipe.builder;

import com.mojang.datafixers.util.Either;
import hu.zoldleo.embers.recipe.StampingRecipe;
import hu.zoldleo.embers.recipe.StampingRecipe.TagAmount;

import hu.zoldleo.embers.util.Misc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class StampingRecipeBuilder {
	public ResourceLocation id;
	public Ingredient stamp;
	public Ingredient input = Ingredient.EMPTY;
	public SizedFluidIngredient fluid = Misc.EMPTY_FLUID_INGREDIENT;
	public Either<ItemStack, TagAmount> output;

	public static StampingRecipeBuilder create(ItemStack itemStack) {
		StampingRecipeBuilder builder = new StampingRecipeBuilder();
		builder.output = Either.left(itemStack);
		builder.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return builder;
	}

	public static StampingRecipeBuilder create(Item item) {
		return create(new ItemStack(item));
	}

	public static StampingRecipeBuilder create(TagKey<Item> tag, int amount) {
		StampingRecipeBuilder builder = new StampingRecipeBuilder();
		builder.output = Either.right(new TagAmount(tag, amount));
		builder.id = tag.location();
		return builder;
	}

	public static StampingRecipeBuilder create(TagKey<Item> tag) {
		return create(tag, 1);
	}

	public StampingRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public StampingRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public StampingRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public StampingRecipeBuilder stamp(Ingredient stamp) {
		this.stamp = stamp;
		return this;
	}

	public StampingRecipeBuilder stamp(ItemLike... stamp) {
		stamp(Ingredient.of(stamp));
		return this;
	}

	public StampingRecipeBuilder stamp(TagKey<Item> tag) {
		stamp(Ingredient.of(tag));
		return this;
	}

	public StampingRecipeBuilder input(Ingredient input) {
		this.input = input;
		return this;
	}

	public StampingRecipeBuilder input(ItemLike... input) {
		input(Ingredient.of(input));
		return this;
	}

	public StampingRecipeBuilder input(TagKey<Item> tag) {
		input(Ingredient.of(tag));
		return this;
	}

	public StampingRecipeBuilder fluid(SizedFluidIngredient fluid) {
		this.fluid = fluid;
		return this;
	}

	public StampingRecipeBuilder fluid(Fluid fluid, int amount) {
		fluid(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public StampingRecipeBuilder fluid(FluidStack stack) {
		fluid(SizedFluidIngredient.of(stack));
		return this;
	}

	public StampingRecipeBuilder fluid(TagKey<Fluid> fluid, int amount) {
		fluid(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public StampingRecipe build() {
		return new StampingRecipe(stamp, input, fluid, output);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}