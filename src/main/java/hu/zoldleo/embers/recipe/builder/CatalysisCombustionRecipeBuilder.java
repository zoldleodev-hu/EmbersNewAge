package hu.zoldleo.embers.recipe.builder;

import hu.zoldleo.embers.RegistryManager;

import hu.zoldleo.embers.recipe.CatalysisCombustionRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class CatalysisCombustionRecipeBuilder {
	public ResourceLocation id;
	public Ingredient ingredient;
	public Ingredient machine;
	public int burnTime;
	public double multiplier;

	public static CatalysisCombustionRecipeBuilder create(Ingredient ingredient) {
		CatalysisCombustionRecipeBuilder builder = new CatalysisCombustionRecipeBuilder();
		builder.ingredient = ingredient;
		return builder;
	}

	public static CatalysisCombustionRecipeBuilder create(TagKey<Item> tag) {
		CatalysisCombustionRecipeBuilder builder = create(Ingredient.of(tag));
		builder.id = tag.location();
		return builder;
	}

	public static CatalysisCombustionRecipeBuilder create(ItemStack itemStack) {
		CatalysisCombustionRecipeBuilder builder = create(Ingredient.of(itemStack));
		builder.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return builder;
	}

	public static CatalysisCombustionRecipeBuilder create(Item item) {
		return create(new ItemStack(item));
	}

	public CatalysisCombustionRecipeBuilder id(ResourceLocation id) {
		this.id = id;
		return this;
	}

	public CatalysisCombustionRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public CatalysisCombustionRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public CatalysisCombustionRecipeBuilder catalysis() {
		this.machine = Ingredient.of(RegistryManager.CATALYSIS_CHAMBER_ITEM.get());
		return this;
	}

	public CatalysisCombustionRecipeBuilder combustion() {
		this.machine = Ingredient.of(RegistryManager.COMBUSTION_CHAMBER_ITEM.get());
		return this;
	}

	public CatalysisCombustionRecipeBuilder burnTime(int burnTime) {
		this.burnTime = burnTime;
		return this;
	}

	public CatalysisCombustionRecipeBuilder multiplier(double multiplier) {
		this.multiplier = multiplier;
		return this;
	}

	public CatalysisCombustionRecipe build() {
		return new CatalysisCombustionRecipe(ingredient, machine, burnTime, multiplier);
	}

	public void save(RecipeOutput output) {
        output.accept(id, build(), null);
	}
}