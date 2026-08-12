package hu.zoldleo.embers.recipe.builder;

import java.util.ArrayList;
import java.util.Arrays;

import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.AlchemyRecipe;
import hu.zoldleo.embers.recipe.base.AlchemyRecipeBase;
import hu.zoldleo.embers.recipe.AlchemyRecipeForBabies;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class AlchemyRecipeBuilder implements RecipeBuilder {
    public ResourceLocation id;
	public String group;
	public ItemStack output;
	public ItemStack failure = RegistryManager.ALCHEMICAL_WASTE.toStack();
	public Ingredient tablet;
	public ArrayList<Ingredient> aspects = new ArrayList<>();
	public ArrayList<Ingredient> inputs = new ArrayList<>();
	public boolean babbyGames = false;

	public static AlchemyRecipeBuilder create(ItemStack itemStack) {
		AlchemyRecipeBuilder builder = new AlchemyRecipeBuilder();
		builder.output = itemStack;
        builder.id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return builder;
	}

	public static AlchemyRecipeBuilder create(ItemLike item) {
		return create(new ItemStack(item));
	}

	public @NotNull AlchemyRecipeBuilder group(String group) {
		this.group = group;
		return this;
	}

	public AlchemyRecipeBuilder domain(String domain) {
		this.id = ResourceLocation.fromNamespaceAndPath(domain, this.id.getPath());
		return this;
	}

	public AlchemyRecipeBuilder folder(String folder) {
		this.id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath());
		return this;
	}

	public AlchemyRecipeBuilder tablet(Ingredient tablet) {
		this.tablet = tablet;
		return this;
	}

	public AlchemyRecipeBuilder tablet(ItemLike... tablet) {
		tablet(Ingredient.of(tablet));
		return this;
	}

	public AlchemyRecipeBuilder tablet(TagKey<Item> tag) {
		tablet(Ingredient.of(tag));
		return this;
	}

	public AlchemyRecipeBuilder output(ItemStack output) {
		this.output = output;
		return this;
	}

	public AlchemyRecipeBuilder output(Item item) {
		output(new ItemStack(item));
		return this;
	}

	public AlchemyRecipeBuilder failure(ItemStack failure) {
		this.failure = failure;
		return this;
	}

	public AlchemyRecipeBuilder failure(Item item) {
		return failure(new ItemStack(item));
	}

	public AlchemyRecipeBuilder aspects(ArrayList<Ingredient> aspects) {
		this.aspects = aspects;
		return this;
	}

	public AlchemyRecipeBuilder inputs(ArrayList<Ingredient> inputs) {
		this.inputs = inputs;
		return this;
	}

	public AlchemyRecipeBuilder aspects(Ingredient... aspects) {
        this.aspects.addAll(Arrays.asList(aspects));
		return this;
	}

	public AlchemyRecipeBuilder inputs(Ingredient... inputs) {
        this.inputs.addAll(Arrays.asList(inputs));
		return this;
	}

	public AlchemyRecipeBuilder aspects(ItemLike... aspects) {
		for (ItemLike aspect : aspects)
			this.aspects.add(Ingredient.of(aspect));
		return this;
	}

	public AlchemyRecipeBuilder inputs(ItemLike... inputs) {
		for (ItemLike input : inputs)
			this.inputs.add(Ingredient.of(input));
		return this;
	}

	@SafeVarargs
	public final AlchemyRecipeBuilder aspects(TagKey<Item>... aspects) {
		for (TagKey<Item> aspect : aspects)
			this.aspects.add(Ingredient.of(aspect));
		return this;
	}

	@SafeVarargs
	public final AlchemyRecipeBuilder inputs(TagKey<Item>... inputs) {
		for (TagKey<Item> input : inputs)
			this.inputs.add(Ingredient.of(input));
		return this;
	}

	public AlchemyRecipeBuilder setBabbyGames(boolean babbyGames) { // TODO: config
		this.babbyGames = babbyGames;
		return this;
	}

	public AlchemyRecipeBase build() {
		if (babbyGames)
			return new AlchemyRecipeForBabies(tablet, aspects, inputs, output, failure);
		return new AlchemyRecipe(tablet, aspects, inputs, output, failure);
	}

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String s, @NotNull Criterion<?> criterion) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return output.getItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        Advancement.Builder advancement$builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(AdvancementRequirements.Strategy.OR);
        recipeOutput.accept(id, build(), advancement$builder.build(id.withPrefix("recipes/misc/")));
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        Advancement.Builder advancement$builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(AdvancementRequirements.Strategy.OR);
        recipeOutput.accept(id, build(), advancement$builder.build(id.withPrefix("recipes/misc/")));
    }
}