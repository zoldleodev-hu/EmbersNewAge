package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.recipe.MeltingRecipe;
import hu.zoldleo.embers.util.FluidOutput;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class MeltingRecipeBuilder {
	public ResourceLocation id;
	public Ingredient ingredient;
	public FluidOutput output;
	public FluidOutput bonus = FluidOutput.EMPTY;

	public static MeltingRecipeBuilder create(Ingredient ingredient) {
		MeltingRecipeBuilder builder = new MeltingRecipeBuilder();
		builder.ingredient = ingredient;
		return builder;
	}

	public static MeltingRecipeBuilder create(TagKey<Item> tag) {
		MeltingRecipeBuilder builder = create(Ingredient.of(tag));
		builder.id = tag.location();
		return builder;
	}

	public static MeltingRecipeBuilder create(ItemStack itemStack) {
		MeltingRecipeBuilder builder = create(Ingredient.of(itemStack));
		builder.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return builder;
	}

	public static MeltingRecipeBuilder create(Item item) {
		return create(new ItemStack(item));
	}

	public MeltingRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public MeltingRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public MeltingRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public MeltingRecipeBuilder bonusName(String stuff) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_" + stuff);
		return this;
	}

	public MeltingRecipeBuilder output(FluidStack output) {
		this.output = new FluidOutput(output);
		return this;
	}

	public MeltingRecipeBuilder output(Fluid fluid, int amount) {
		output(new FluidStack(fluid, amount));
		return this;
	}

	public MeltingRecipeBuilder output(TagKey<Fluid> tag, int amount) {
		this.output = new FluidOutput(tag, amount);
		return this;
	}

	public MeltingRecipeBuilder bonus(FluidStack bonus) {
		this.bonus = new FluidOutput(bonus);
		return this;
	}

	public MeltingRecipeBuilder bonus(Fluid fluid, int amount) {
		bonus(new FluidStack(fluid, amount));
		return this;
	}

	public MeltingRecipeBuilder bonus(TagKey<Fluid> tag, int amount) {
		this.bonus = new FluidOutput(tag, amount);
		return this;
	}

	public MeltingRecipe build() {
		return new MeltingRecipe(ingredient, output, bonus);
	}

	public void save(RecipeOutput output) {
		output.accept(id, build(), null);
	}
}