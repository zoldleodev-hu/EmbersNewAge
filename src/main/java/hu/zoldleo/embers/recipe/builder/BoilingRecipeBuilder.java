package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.recipe.BoilingRecipe;
import hu.zoldleo.embers.util.FluidOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class BoilingRecipeBuilder {
	public ResourceLocation id;
	public SizedFluidIngredient input;
	public FluidOutput output;

	public static BoilingRecipeBuilder create(FluidStack fluidStack) {
		BoilingRecipeBuilder builder = new BoilingRecipeBuilder();
		builder.output = new FluidOutput(fluidStack);
		builder.id = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid());
		return builder;
	}

	public static BoilingRecipeBuilder create(Fluid fluid, int amount) {
		return create(new FluidStack(fluid, amount));
	}

	public static BoilingRecipeBuilder create(TagKey<Fluid> tag, int amount) {
		BoilingRecipeBuilder builder = new BoilingRecipeBuilder();
		builder.output = new FluidOutput(tag, amount);
		builder.id = tag.location();
		return builder;
	}

	public BoilingRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public BoilingRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public BoilingRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public BoilingRecipeBuilder input(SizedFluidIngredient fluid) {
		this.input = fluid;
		return this;
	}

	public BoilingRecipeBuilder input(Fluid fluid, int amount) {
		input(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public BoilingRecipeBuilder input(FluidStack stack) {
		input(SizedFluidIngredient.of(stack));
		return this;
	}

	public BoilingRecipeBuilder input(TagKey<Fluid> fluid, int amount) {
		input(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public BoilingRecipeBuilder output(FluidStack output) {
		this.output = new FluidOutput(output);
		return this;
	}

	public BoilingRecipeBuilder output(Fluid fluid, int amount) {
		output(new FluidStack(fluid, amount));
		return this;
	}

	public BoilingRecipeBuilder output(TagKey<Fluid> tag, int amount) {
		this.output = new FluidOutput(tag, amount);
		return this;
	}

	public BoilingRecipe build() {
		return new BoilingRecipe(input, output);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}