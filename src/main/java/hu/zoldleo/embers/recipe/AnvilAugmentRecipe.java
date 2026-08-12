package hu.zoldleo.embers.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hu.zoldleo.embers.RegistryManager;
import hu.zoldleo.embers.recipe.base.IDawnstoneAnvilRecipe;
import hu.zoldleo.embers.recipe.base.IVisuallySplitRecipe;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import hu.zoldleo.embers.api.augment.AugmentUtil;
import hu.zoldleo.embers.api.augment.IAugment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AnvilAugmentRecipe implements IDawnstoneAnvilRecipe, IVisuallySplitRecipe<IDawnstoneAnvilRecipe> {
	public static final Serializer SERIALIZER = new Serializer();

	public final Ingredient tool;
	public final Ingredient input;
	public final Holder<IAugment> augment;

	public static List<IDawnstoneAnvilRecipe> visualRecipes = new ArrayList<>();

	public AnvilAugmentRecipe(Ingredient tool, Ingredient input, Holder<IAugment> augment) {
		this.tool = tool;
		this.input = input;
		this.augment = augment;
	}

	@Override
	public boolean matches(@NotNull RecipeInput context, @NotNull Level pLevel) {
		if (augment.value().countTowardsTotalLevel()) {
			ItemStack toolStack = context.getItem(0);
			return tool.test(toolStack) && input.test(context.getItem(1)) && AugmentUtil.getLevel(toolStack) > AugmentUtil.getTotalAugmentLevel(toolStack);
		} else 
			return tool.test(context.getItem(0)) && input.test(context.getItem(1));
	}

	@Override
	public List<ItemStack> getOutput(RecipeInput context) {
		ItemStack result = context.getItem(0).copy();
		AugmentUtil.addAugment(result, context.getItem(1), augment);
		return List.of(result);
	}

	@Override
	public List<IDawnstoneAnvilRecipe> getVisualRecipes() {
		visualRecipes.clear();
		for (ItemStack stack : tool.getItems()) {
			ItemStack leveledTool = stack.copy();
			if (augment.value().countTowardsTotalLevel()) {
				AugmentUtil.setLevel(leveledTool, AugmentUtil.getLevel(leveledTool) + 1);
			}
			ItemStack augmentedTool = leveledTool.copy();
			AugmentUtil.addAugment(augmentedTool, ItemStack.EMPTY, augment);
			visualRecipes.add(new AnvilDisplayRecipe(List.of(augmentedTool), List.of(leveledTool), input));
		}
		return visualRecipes;
	}

	@Override
	public List<ItemStack> getDisplayInputBottom() {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayInputTop() {
		return List.of();
	}

	@Override
	public List<ItemStack> getDisplayOutput() {
		return List.of();
	}

	@Override
	public @NotNull RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<AnvilAugmentRecipe> {
        private static final MapCodec<AnvilAugmentRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("tool").forGetter(recipe -> recipe.tool),
                Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                RegistryManager.AUGMENT_REGISTRY.holderByNameCodec().fieldOf("augment").forGetter(recipe -> recipe.augment)
        ).apply(instance, AnvilAugmentRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AnvilAugmentRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.tool,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.input,
                ByteBufCodecs.holderRegistry(RegistryManager.AUGMENT_REGISTRY_KEY), recipe -> recipe.augment,
                AnvilAugmentRecipe::new
        );

        @Override
        public @NotNull MapCodec<AnvilAugmentRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AnvilAugmentRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}