package hu.zoldleo.embers.recipe.builder;

import java.util.ArrayList;

import hu.zoldleo.embers.recipe.MixingRecipe;
import hu.zoldleo.embers.util.FluidOutput;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class MixingRecipeBuilder {
	public ResourceLocation id;
	public ArrayList<SizedFluidIngredient> inputs = new ArrayList<>();
	public FluidOutput output;

	public static MixingRecipeBuilder create(FluidStack fluidStack) {
		MixingRecipeBuilder builder = new MixingRecipeBuilder();
		builder.output = new FluidOutput(fluidStack);
		builder.id = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid());
		return builder;
	}

	public static MixingRecipeBuilder create(Fluid fluid, int amount) {
		return create(new FluidStack(fluid, amount));
	}

	public static MixingRecipeBuilder create(TagKey<Fluid> tag, int amount) {
		MixingRecipeBuilder builder = new MixingRecipeBuilder();
		builder.output = new FluidOutput(tag, amount);
		builder.id = tag.location();
		return builder;
	}

	public MixingRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public MixingRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public MixingRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public MixingRecipeBuilder input(SizedFluidIngredient fluid) {
		this.inputs.add(fluid);
		return this;
	}

	public MixingRecipeBuilder input(Fluid fluid, int amount) {
		input(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public MixingRecipeBuilder input(FluidStack stack) {
		input(SizedFluidIngredient.of(stack));
		return this;
	}

	public MixingRecipeBuilder input(TagKey<Fluid> fluid, int amount) {
		input(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public MixingRecipeBuilder output(FluidStack output) {
		this.output = new FluidOutput(output);
		return this;
	}

	public MixingRecipeBuilder output(Fluid fluid, int amount) {
		output(new FluidStack(fluid, amount));
		return this;
	}

	public MixingRecipeBuilder output(TagKey<Fluid> tag, int amount) {
		this.output = new FluidOutput(tag, amount);
		return this;
	}

	public MixingRecipe build() {
		return new MixingRecipe(inputs, output);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}