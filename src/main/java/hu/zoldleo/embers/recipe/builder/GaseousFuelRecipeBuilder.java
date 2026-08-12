package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.recipe.GaseousFuelRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class GaseousFuelRecipeBuilder {
	public ResourceLocation id;
	public SizedFluidIngredient input;
	public int burnTime;
	public double powerMultiplier;

	public static GaseousFuelRecipeBuilder create(ResourceLocation id) {
		GaseousFuelRecipeBuilder builder = new GaseousFuelRecipeBuilder();
		builder.id = id;
		return builder;
	}

	public static GaseousFuelRecipeBuilder create(FluidStack fluidStack) {
		GaseousFuelRecipeBuilder builder = create(BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()));
		builder.input = SizedFluidIngredient.of(fluidStack);
		return builder;
	}

	public static GaseousFuelRecipeBuilder create(Fluid fluid, int amount) {
		return create(new FluidStack(fluid, amount));
	}

	public GaseousFuelRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public GaseousFuelRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public GaseousFuelRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public GaseousFuelRecipeBuilder input(SizedFluidIngredient fluid) {
		this.input = fluid;
		return this;
	}

	public GaseousFuelRecipeBuilder input(Fluid fluid, int amount) {
		input(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public GaseousFuelRecipeBuilder input(FluidStack stack) {
		input(SizedFluidIngredient.of(stack));
		return this;
	}

	public GaseousFuelRecipeBuilder input(TagKey<Fluid> fluid, int amount) {
		input(SizedFluidIngredient.of(fluid, amount));
		return this;
	}

	public GaseousFuelRecipeBuilder burnTime(int burnTime) {
		this.burnTime = burnTime;
		return this;
	}

	public GaseousFuelRecipeBuilder powerMultiplier(double powerMultiplier) {
		this.powerMultiplier = powerMultiplier;
		return this;
	}

	public GaseousFuelRecipe build() {
		return new GaseousFuelRecipe(input, burnTime, powerMultiplier);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}